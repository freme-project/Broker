package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.conversion.rdf.RDFConversionService;

@RestController
public class TildeETranslate extends BaseRestController {

	@Autowired
	RDFConversionService rdfConversionService;

	private final String INPUT_TYPE_NIF_TURTLE = "nif/turtle";
	private final String INPUT_TYPE_PLAINTEXT = "plaintext";

	public void setAppId(String appId) {
		this.appId = appId;
	}

	@Value("${tildemt.appid}")
	private String appId;

	private String endpoint = "https://letsmt.eu/ws/Service.svc/json/Translate?appid={appid}&systemid={systemid}&text={text}";
	
	@RequestMapping("/e-translate/tilde")
	public ResponseEntity<String> tildeTranslate(
			@RequestParam(value = "input") String input,
			@RequestParam(value = "input-type") String inputType,
			@RequestParam(value = "client-id") String clientId,
			@RequestParam(value = "source-lang") String sourceLang,
			@RequestParam(value = "target-lang") String targetLang,
			@RequestParam(value = "translation-system-id") String translationSystemId,
			@RequestParam(value = "domain", defaultValue = "") String domain,
			@RequestHeader("Accept") String acceptHeader ) {

		// input validation - validation for required parameters is done by
		// spring mvc
		inputType = inputType.toLowerCase();
		if (!(inputType.equals(INPUT_TYPE_NIF_TURTLE) || inputType
				.equals(INPUT_TYPE_PLAINTEXT))) {
			return new ResponseEntity<String>(
					"valid input type is required to be "
							+ String.join(",", INPUT_TYPE_NIF_TURTLE,
									INPUT_TYPE_PLAINTEXT),
					HttpStatus.BAD_REQUEST);
		}

		// extract plaintext
		String plaintext = null;
		Model model = ModelFactory.createDefaultModel();
		Resource sourceResource = null;
		
		if (inputType.equals("INPUT_TYPE_NIF")) {
			// TODO validate NIF
			// TODO extract plaintext + provenance information from NIF
			// TODO tbd: what happens if there are multiple strings in the
			// source document

			plaintext = "";
		} else {
			plaintext = input;
			sourceResource = rdfConversionService.plaintextToRDF(model, plaintext, sourceLang);
		}

		// send request to tilde mt
		String translation = null;
		try {
			HttpResponse<String> response = Unirest.get(endpoint)
					.routeParam("appid", appId)
					.routeParam("systemid", translationSystemId)
					.routeParam("text", plaintext)
					.header("client-id", clientId)
					.header("Content-type", "application/rdf-xml").asString();

			if (response.getStatus() != HttpStatus.OK.value()) {
				return externalServiceFailedResponse();
			}

			translation = response.getBody();

			// strip leading and trailing slash
			if (translation.length() > 1) {
				translation = translation
						.substring(1, translation.length() - 1);
			}
		} catch (UnirestException e) {
			return externalServiceFailedResponse();
		}
		
		rdfConversionService.addTranslation(translation, sourceResource, targetLang);
		
		System.err.println(acceptHeader);
		
		// get output format
		RDFConversionService.RDFSerialization outputFormat = getOutputSerialization(acceptHeader);
		String serialization = rdfConversionService.serializeRDF(model, outputFormat);
		
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
