package eu.freme.broker.eservices;

import io.swagger.annotations.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.ExternalServiceFailedException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.conversion.etranslate.TranslationConversionService;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;

/**
 * REST controller for Tilde e-Translation service
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RestController
@Api(value = "e-Translation")

public class TildeETranslation extends BaseRestController {

	@Autowired
	TranslationConversionService translationConversionService;

	private String endpoint = "https://services.tilde.com/translation/?sourceLang={source-lang}&targetLang={target-lang}";

	//private final String requestFormatValuesShort = "text, json-ld, turtle";
	//private final String[] requestFormatValuesMime = {"text/plain", "text/turtle", "application/json+ld"};

	@ApiOperation(value = "Perform machine translation using Tilde's API",
	    notes = "Parameters can be submitted via URL or via form-data post body. A list of available language pairs is [here](https://services.tilde.com/translationsystems).")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful response"),
			@ApiResponse(code = 406, message = "Bad request - input validation failed") })
	@RequestMapping(value = "/e-translation/tilde",
			method = RequestMethod.POST,
			produces = {"text/turtle", "application/json+ld", "application/n-triples", "application/rdf+xml", "text/n3"},
			consumes = {"text/plain", "text/turtle", "application/json+ld"})//, "application/n-triples", "application/rdf+xml","text/n3"})

	public ResponseEntity<String> tildeTranslate(

			@ApiParam(value="The string to be translated. Can be either plaintext or NIF (see parameter informat). Short form is i. If set, it will be prefered over the body content.")
			@RequestParam(value = "input", required = false) String input,
			@ApiParam(value="HIDDEN") @RequestParam(value = "i", required = false) String i,

			@ApiParam(value="Format of input string. Can be \"text\", \"json-ld\", \"turtle\". Defaults to \"turtle\". This parameter overrides Content-Type header. Short form is f.",
					allowableValues = "text, json-ld, turtle",
					defaultValue = "turtle")
			@RequestParam(value = "informat", required = false) String informat,
			@ApiParam(value="HIDDEN") @RequestParam(value = "f", required = false) String f,
			
			@ApiParam(value="Format of output string. Can be \"text\", \"json-ld\", \"turtle\". Defaults to \"turtle\". This parameter overrides Accept header. Short form is o.",
					allowableValues = "json-ld, turtle",
					defaultValue = "turtle")
			@RequestParam(value = "outformat", required = false) String outformat,
			@ApiParam(value="HIDDEN") @RequestParam(value = "o", required = false) String o,

			@ApiParam(value="Controls the url of rdf resources generated from plaintext. Has default value \"http://freme-project.eu/\". Short form is p.")
			@RequestParam(value = "prefix", required = false) String prefix,
			@ApiParam(value="HIDDEN") @RequestParam(value = "p", required = false) String p,

			//@ApiParam(value="Format of output. Can be \"text\", \"json-ld\", \"turtle\". Defaults to \"turtle\". The parameter *outformat* overrides Accept header.",
			//		allowableValues = requestFormatValuesShort,
			//		defaultValue = "turtle")
			@RequestHeader(value = "Accept", required = false) String acceptHeader,

			//@ApiParam(value="Format of input string. Can be \"text\", \"json-ld\", \"turtle\". Defaults to \"turtle\". The parameter *informat* overrides Content-Type header.",
			//		allowableValues = requestFormatValuesShort,
			//		defaultValue = "turtle")
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,

			@ApiParam(value="The string to be translated. Can be either plaintext or NIF. Will be overwritten by parameter input, if set. The format of the body can be \"text/plain\", \"text/turtle\", \"application/json+ld\". Defaults to \"text/turtle\". The parameter *informat* overrides the Content-Type.")
			@RequestBody(required = false) String postBody,

			@ApiParam(value="Source language, e.g. \"en\". A list of available language pairs is [here](https://services.tilde.com/translationsystems).",
					allowableValues = "en,de,fr,nl,it,es")
			@RequestParam(value = "source-lang") String sourceLang,

			@ApiParam(value="Target language, e.g. \"de\". A list of available language pairs is [here](https://services.tilde.com/translationsystems).",
					allowableValues = "en,de,fr,nl,it,es")
			@RequestParam(value = "target-lang") String targetLang,
			
			@ApiParam(value="Currently not used")
			@RequestParam(value = "domain", defaultValue = "", required=false) String domain) {

		// merge long and short parameters - long parameters override short
		// parameters
		if (input == null) {
			input = i;
		}
		if (informat == null) {
			informat = f;
		}
		if (outformat == null) {
			outformat = o;
		}
		if (prefix == null) {
			prefix = p;
		}
		NIFParameterSet parameters = this.normalizeNif(input, informat,
				outformat, postBody, acceptHeader, contentTypeHeader, prefix);

		// create rdf model
		String plaintext = null;
		Model inputModel = ModelFactory.createDefaultModel();

		if (!parameters.getInformat().equals(
				RDFConstants.RDFSerialization.PLAINTEXT)) {
			// input is nif
			try {
				inputModel = this.unserializeNif(parameters.getInput(),
						parameters.getInformat());
			} catch (Exception e) {
				logger.error("failed", e);
				throw new BadRequestException("Error parsing NIF input");
			}
		} else {
			// input is plaintext
			plaintext = parameters.getInput();
			rdfConversionService.plaintextToRDF(inputModel, plaintext,
					sourceLang, parameters.getPrefix());
		}

		// send request to tilde mt
		Model responseModel = null;
		try {
			HttpResponse<String> response = Unirest
					.post(endpoint)
					.routeParam("source-lang", sourceLang)
					.routeParam("target-lang", targetLang)
					.header("Accept", "application/x-turtle")
					.header("Content-Type", "application/x-turtle")
					.body(rdfConversionService.serializeRDF(inputModel,
							RDFSerialization.TURTLE)).asString();

			if (response.getStatus() != HttpStatus.OK.value()) {
				throw new ExternalServiceFailedException(
						"External service failed with status code "
								+ response.getStatus(),
						HttpStatus.valueOf(response.getStatus()));
			}

			String translation = response.getBody();

			responseModel = rdfConversionService.unserializeRDF(translation,
					RDFSerialization.TURTLE);

		} catch (Exception e) {
			if (e instanceof ExternalServiceFailedException) {
				throw new ExternalServiceFailedException(e.getMessage(),
						((ExternalServiceFailedException) e)
								.getHttpStatusCode());
			} else {
				throw new ExternalServiceFailedException(e.getMessage());
			}
		}

		// get output format
		String serialization;
		try {
			serialization = rdfConversionService.serializeRDF(responseModel,
					parameters.getOutformat());
		} catch (Exception e) {
			logger.error("failed", e);
			throw new InternalServerErrorException("internal server error");
		}

		return new ResponseEntity<String>(serialization, HttpStatus.OK);
	}
}
