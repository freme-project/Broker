package eu.freme.broker.eservices;

import io.swagger.annotations.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
@Produces({"application/json", "application/xml"})

public class TildeETranslation extends BaseRestController {

	@Autowired
	TranslationConversionService translationConversionService;

	private String endpoint = "https://services.tilde.com/translation/?sourceLang={source-lang}&targetLang={target-lang}";

	@RequestMapping(value = "/e-translation/tilde", method = RequestMethod.POST)
	@POST

	 @ApiOperation(value = "Translate from source-language to target-language",
	    notes = "Perform machine translation with Tilde's API.",
	    responseContainer = "List")
     @ApiResponses(value = { @ApiResponse(code = 400, message = "Insert message"),
	    @ApiResponse(code = 404, message = "Insert message") })

	public ResponseEntity<String> tildeTranslate(

			@ApiParam(value="The string to be translated. Short form is i.") 
			@RequestParam(value = "input", required = false) String input,
			@ApiParam(name="HIDDEN") @RequestParam(value = "i", required = false) String i,

			@ApiParam(value="Format of input string. Can be \"plaintext\", \"json-ld\", \"turtle\". Defaults to \"turtle\". This parameter overrides Content-Type header. Short form is f.")
			@RequestParam(value = "informat", required = false) String informat,
			@ApiParam(name="HIDDEN") @RequestParam(value = "f", required = false) String f,
			
			@ApiParam(value="Format of output string. Can be \"plaintext\", \"json-ld\", \"turtle\". Defaults to \"turtle\". This parameter overrides Accept header. Short form is o.")
			@RequestParam(value = "outformat", required = false) String outformat,
			@ApiParam(name="HIDDEN") @RequestParam(value = "o", required = false) String o,

			@ApiParam(value="Controls the url of rdf resources generated from plaintext. Has default value \"http://freme-project.eu/\"")
			@RequestParam(value = "prefix", required = false) String prefix,
			@ApiParam(name="HIDDEN") @RequestParam(value = "p", required = false) String p,

			@ApiParam(value="Format of output. Can be \"plaintext\", \"json-ld\", \"turtle\". Defaults to \"turtle\".")
			@RequestHeader(value = "Accept", required = false) String acceptHeader,

			@ApiParam(value="Format of input string. Can be \"plaintext\", \"json-ld\", \"turtle\". Defaults to \"turtle\".")
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,

			@RequestBody(required = false) String postBody,

			@ApiParam(value="Source language to be translated from, e.g. \"en\".") @RequestParam(value = "source-lang") String sourceLang,

			@ApiParam(value="Target language to be translated to, e.g. \"de\".") @RequestParam(value = "target-lang") String targetLang,
			
			@ApiParam(value="Currently not used", required=false)
			@RequestParam(value = "domain", defaultValue = "") String domain) {

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
			try {
				plaintext = URLDecoder.decode(plaintext, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e);
				throw new InternalServerErrorException(e.getMessage());
			}
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
