package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.ExternalServiceFailedException;
import eu.freme.conversion.etranslate.TranslationConversionService;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;

@RestController
public class TildeETranslation extends BaseRestController {

	@Autowired
	RDFConversionService rdfConversionService;

	@Autowired
	TranslationConversionService translationConversionService;

	public void setAppId(String appId) {
		this.appId = appId;
	}

	@Value("${tildemt.appid}")
	private String appId;

	private String endpoint = "https://letsmt.eu/ws/Service.svc/json/Translate?appid={appid}&systemid={systemid}&text={text}";

	@RequestMapping(value = "/e-translate/tilde", method = RequestMethod.POST)
	public ResponseEntity<String> tildeTranslate(
			@RequestParam(value = "input", required = false) String input,
			@RequestParam(value = "client-id") String clientId,
			@RequestParam(value = "source-lang") String sourceLang,
			@RequestParam(value = "target-lang") String targetLang,
			@RequestParam(value = "translation-system-id") String translationSystemId,
			@RequestParam(value = "domain", defaultValue = "") String domain,
			@RequestHeader(value = "Accept", defaultValue = "") String acceptHeader,
			@RequestHeader(value = "Content-Type", defaultValue = "") String contentType,
			@RequestBody(required = false) String postBody) {

		validateContentType(contentType);

		// create rdf model
		String plaintext = null;
		Model model = ModelFactory.createDefaultModel();
		Resource sourceResource = null;

		if (!contentType.equals(inputTypePlaintext)) {
			// input is nif
			try {
				model = unserializeNIF(postBody, contentType);
				sourceResource = translationConversionService
						.extractTextToTranslate(model);

				if (sourceResource == null) {
					throw new BadRequestException(
							"No text to translate could be found in input.");
				}
				Property isString = model.getProperty(RDFConstants.nifPrefix
						+ "isString");
				plaintext = sourceResource.getProperty(isString).getObject()
						.asLiteral().getString();
			} catch (Exception e) {
				logger.error("failed", e);
				throw new BadRequestException("Error parsing input");
			}
		} else {
			// input is plaintext
			plaintext = input;
			sourceResource = rdfConversionService.plaintextToRDF(model,
					plaintext, sourceLang);
		}

		// send request to tilde mt
		String translation = null;
		try {
			System.out.println(plaintext);
			HttpResponse<String> response = Unirest.get(endpoint)
					.routeParam("appid", appId)
					.routeParam("systemid", translationSystemId)
					.routeParam("text", plaintext)
					.header("client-id", clientId)
					.header("Content-type", "application/rdf-xml").asString();

			if (response.getStatus() != HttpStatus.OK.value()) {
				if( response.getStatus() == HttpStatus.BAD_REQUEST.value() ){
					throw new BadRequestException("external service has failed: " + response.getBody());
				} else{
					throw new ExternalServiceFailedException();
				}
			}

			translation = response.getBody();

			// strip leading and trailing slash
			if (translation.length() > 1) {
				translation = translation
						.substring(1, translation.length() - 1);
			}
		} catch (UnirestException e) {
			throw new ExternalServiceFailedException();
		}

		translationConversionService.addTranslation(translation,
				sourceResource, targetLang);

		// get output format
		RDFConstants.RDFSerialization outputFormat = getOutputSerialization(acceptHeader);
		String serialization;
		try {
			serialization = rdfConversionService.serializeRDF(model,
					outputFormat);
		} catch (Exception e) {
			logger.error("failed", e);
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<String>(serialization, HttpStatus.OK);
	}

	public RDFConversionService getRdfConversionService() {
		return rdfConversionService;
	}

	public void setRdfConversionService(
			RDFConversionService rdfConversionService) {
		this.rdfConversionService = rdfConversionService;
	}
}
