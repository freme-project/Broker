package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.eservices.elink.DataEnricher;
import eu.freme.eservices.elink.exceptions.TemplateNotFoundException;

@RestController
public class ELink extends BaseRestController {

	@Autowired
	DataEnricher dataEnricher;

	@RequestMapping(value = "/e-link/", method = RequestMethod.POST)
	public ResponseEntity<String> enrich(
			@RequestParam(value = "inputType") String inputType,
			@RequestParam(value = "templateid", defaultValue = "") int templateId,
			@RequestParam(value = "input") String input,
			@RequestHeader(value = "Accept") String acceptHeader) {

		if (!validateInputType(inputType)) {
			return new ResponseEntity<String>("Invalid input-type",
					HttpStatus.BAD_REQUEST);
		}

		Model model = ModelFactory.createDefaultModel();

		if (!inputType.equals(inputTypePlaintext)) {
			try {
				model = unserializeNIF(input, inputType);
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<String>("invalid nif",
						HttpStatus.BAD_REQUEST);
			}
		}

		try {
			dataEnricher.enrichNIF(model, templateId);
		} catch (TemplateNotFoundException e) {
			return new ResponseEntity<String>("template not found",
					HttpStatus.BAD_REQUEST);
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
}
