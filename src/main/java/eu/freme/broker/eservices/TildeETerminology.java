package eu.freme.broker.eservices;

import eu.freme.broker.tools.NIFParameterFactory;
import io.swagger.annotations.*;
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
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.ExternalServiceFailedException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;

/**
 * REST controller for Tilde e-Terminology service
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RestController
@Api("e-Terminology")
public class TildeETerminology extends BaseRestController {

	private String endpoint = "https://services.tilde.com/Terminology/";
	@ApiOperation(notes = "Annotate text with terminology information using Tilde Terminology service.",
			value = "Annotate text with terminology information using Tilde Terminology service.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful response"),
			@ApiResponse(code = 400, message = "Bad request - input validation failed")})
	@RequestMapping(value = "/e-terminology/tilde",
			method = RequestMethod.POST,
			produces = {"text/turtle", "application/json+ld", "application/n-triples", "application/rdf+xml", "text/n3"},
			consumes = {"text/plain", "text/turtle", "application/json+ld", "application/n-triples", "application/rdf+xml","text/n3"})
	public ResponseEntity<String> tildeTranslate(
			@ApiParam(value="The text to annotate. Can be either plaintext or NIF (see parameter informat). Short form is i.")
			@RequestParam(value = "input", required = false) String input,
			@ApiParam(value="HIDDEN") @RequestParam(value = "i", required = false) String i,

			@ApiParam(value="Format of input string. Can be "+ NIFParameterFactory.allowedValuesInformat+". Overrides Content-Type header. Short form is f.",
					allowableValues = NIFParameterFactory.allowedValuesInformat,
					defaultValue = "text")
			@RequestParam(value = "informat", required = false) String informat,
			@ApiParam(value="HIDDEN") @RequestParam(value = "f", required = false) String f,

			@ApiParam(value = "RDF serialization format of Output. Can be "+NIFParameterFactory.allowedValuesOutformat+". Defaults to \"turtle\". Overrides Accept Header (Response Content Type). Short form is o.",
					allowableValues = NIFParameterFactory.allowedValuesOutformat,
					defaultValue = "turtle")
			@RequestParam(value = "outformat", required = false) String outformat,
			@ApiParam(value="HIDDEN") @RequestParam(value = "o", required = false) String o,

			@ApiParam("Unused optional Parameter. Short form is p.")
			@RequestParam(value = "prefix", required = false) String prefix,
			@ApiParam(value="HIDDEN") @RequestParam(value = "p", required = false) String p,

			@RequestHeader(value = "Accept", required = false) String acceptHeader,

			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,

			@ApiParam(value="The text to annotate. Will be overwritten by parameter input, if set. The format of the body can be "+NIFParameterFactory.allowedValuesInformatMime+". Defaults to \"text/plain\". The parameter *informat* overrides the Content-Type.")
			@RequestBody(required = false) String postBody,
			@ApiParam(value="Source language, e.g. \"en\". A list of available language pairs is [here](https://services.tilde.com/translationsystems).",
					allowableValues = "en,de,fr,nl,it,es")
			@RequestParam(value = "source-lang") String sourceLang,
			@ApiParam(value="Target language, e.g. \"de\". A list of available language pairs is [here](https://services.tilde.com/translationsystems).",
					allowableValues = "en,de,fr,nl,it,es")
			@RequestParam(value = "target-lang") String targetLang,
			@ApiParam("If given - it filters out by domain proposed terms. Available domains here: https://term.tilde.com/domains (should pass just ID, eg, TaaS-1001, that means Agriculture)")
			@RequestParam(value = "domain", defaultValue = "", required = false) String domain) {

		System.err.println(postBody);
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
			String nifString = rdfConversionService.serializeRDF(inputModel,
					RDFSerialization.TURTLE);
			if( logger.isDebugEnabled() ){
				logger.debug( "send nif to tilde e-terminology: \n" + nifString);
			}
			HttpResponse<String> response = Unirest.post(endpoint)
					.queryString("sourceLang", sourceLang)
					.queryString("targetLang", targetLang)
					.queryString("domain", domain)
					.header("Accept", "application/turtle")
					.header("Content-Type", "application/turtle")
					.body(nifString).asString();

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
