package eu.freme.broker.eservices;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
import eu.freme.broker.exception.InternalServerErrrorException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.conversion.etranslate.TranslationConversionService;
import eu.freme.conversion.rdf.RDFConstants;

/**
 * REST controller for Tilde e-Translation service
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RestController
public class TildeETranslation extends BaseRestController {

	@Autowired
	TranslationConversionService translationConversionService;

	public void setAppId(String appId) {
		this.appId = appId;
	}

	@Value("${tildemt.appid}")
	private String appId;

	private String endpoint = "https://letsmt.eu/ws/Service.svc/json/Translate?appid={appid}&systemid={systemid}&text={text}";

	@RequestMapping(value = "/e-translation/tilde", method = RequestMethod.POST)
	public ResponseEntity<String> tildeTranslate(
			@RequestParam(value = "input", required = false) String input,
			@RequestParam(value = "i", required = false) String i,
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
			@RequestParam(value = "outformat", required = false) String outformat,
			@RequestParam(value = "o", required = false) String o,
			@RequestParam(value = "prefix", required = false) String prefix,
			@RequestParam(value = "p", required = false) String p,
			@RequestHeader(value = "Accept", required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
			@RequestBody(required = false) String postBody,
			@RequestParam(value = "client-id") String clientId,
			@RequestParam(value = "source-lang") String sourceLang,
			@RequestParam(value = "target-lang") String targetLang,
			@RequestParam(value = "translation-system-id") String translationSystemId,
			@RequestParam(value = "domain", defaultValue = "") String domain){

		// merge long and short parameters - long parameters override short parameters
		if( input == null ){
			input = i;
		}
		if( informat == null ){
			informat = f;
		}
		if( outformat == null ){
			outformat = o;
		}
		if( prefix == null ){
			prefix = p;
		}
		NIFParameterSet parameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
		
		// create rdf model
		String plaintext = null;
		Model model = ModelFactory.createDefaultModel();
		Resource sourceResource = null;

		if (!parameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
			// input is nif
			try {
				model = this.unserializeNif(parameters.getInput(), parameters.getInformat());
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
			plaintext = parameters.getInput();
			try {
				plaintext = URLDecoder.decode(plaintext, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e);
				throw new InternalServerErrrorException(e.getMessage());				
			}
			sourceResource = rdfConversionService.plaintextToRDF(model,
					plaintext, sourceLang, parameters.getPrefix());
		}

		// send request to tilde mt
		String translation = null;
		try {
			HttpResponse<String> response = Unirest.get(endpoint)
					.routeParam("appid", appId)
					.routeParam("systemid", translationSystemId)
					.routeParam("text", URLEncoder.encode(plaintext, "UTF-8"))
					.header("client-id", clientId)
					.asString();

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
			logger.error(e);
			throw new ExternalServiceFailedException();
		} catch( UnsupportedEncodingException e){
			logger.error(e);
			throw new BadRequestException(e.getMessage());
		}

		translationConversionService.addTranslation(translation,
				sourceResource, targetLang);

		// get output format
		String serialization;
		try {
			serialization = rdfConversionService.serializeRDF(model,
					parameters.getOutformat());
		} catch (Exception e) {
			logger.error("failed", e);
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<String>(serialization, HttpStatus.OK);
	}

}
