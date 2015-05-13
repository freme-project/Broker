package eu.freme.broker.eservices;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.conversion.etranslate.TranslationConversionService;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;

@RestController
public class TildeETranslation extends BaseRestController {

	@Autowired
	RDFConversionService rdfConversionService;

	@Autowired
	TranslationConversionService translationConversionService;

	private String endpoint = "https://services.tilde.com/translation/?sourceLang={source-lang}&targetLang={target-lang}";

	@RequestMapping(value = "/e-translation/tilde", method = RequestMethod.POST)
	public ResponseEntity<String> tildeTranslate(
			@RequestParam(value = "input") String input,
			@RequestParam(value = "input-type") String inputType,
			@RequestParam(value = "source-lang") String sourceLang,
			@RequestParam(value = "target-lang") String targetLang,
			@RequestParam(value = "domain", defaultValue = "") String domain,
			@RequestHeader(value = "Accept", defaultValue = "") String acceptHeader,
			HttpServletRequest request) {

		if (!validateInputType(inputType)) {
			logger.info("invalid input type");
			return new ResponseEntity<String>("Invalid input-type",
					HttpStatus.BAD_REQUEST);
		}

		// create rdf model
		Model model = ModelFactory.createDefaultModel();

		if (!inputType.equals(inputTypePlaintext)) {
			try {
				model = unserializeNIF(input, inputType);
			} catch (Exception e) {
				logger.error(e);
				return new ResponseEntity<String>("Error parsing input",
						HttpStatus.BAD_REQUEST);
			}
		} else {
			rdfConversionService.plaintextToRDF(model, input, sourceLang);
		}

		// send request to tilde mt
		try {

			// workaround for tilde mt bug
			model.setNsPrefix("itsrdf", "http://www.w3.org/2005/11/its/rdf#");
			HttpResponse<String> response = Unirest
					.post(endpoint)
					.routeParam("source-lang", sourceLang)
					.routeParam("target-lang", targetLang)
					.header("Accept", "application/x-turtle")
					.header("Content-Type", "application/x-turtle")
					.body(rdfConversionService.serializeRDF(model,
							RDFSerialization.TURTLE)).asString();

			if (response.getStatus() != HttpStatus.OK.value()) {
				logger.error("external service failed, response body: "
						+ response.getBody());
				return externalServiceFailedResponse();
			}

			String translation = response.getBody();

			Model responseModel = rdfConversionService.unserializeRDF(
					translation, RDFSerialization.TURTLE);
			model.add(responseModel);
		} catch (Exception e) {
			logger.error(e);
			return externalServiceFailedResponse();
		}

		// get output format
		RDFConstants.RDFSerialization outputFormat = getOutputSerialization(acceptHeader);
		String serialization;
		try {
			serialization = rdfConversionService.serializeRDF(model,
					outputFormat);
		} catch (Exception e) {
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
