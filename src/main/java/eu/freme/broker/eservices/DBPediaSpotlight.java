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

import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;
import eu.freme.eservices.eentity.api.EEntityAPI;
import eu.freme.eservices.eentity.exceptions.ExternalServiceFailedException;

@RestController
public class DBPediaSpotlight extends BaseRestController {

	@Autowired
	EEntityAPI entityAPI;

	@Autowired
	RDFConversionService rdfConversionService;

	@RequestMapping(value = "/e-entity/dbpedia-spotlight", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> execute(
			@RequestParam(value = "text", defaultValue = "") String text,
			@RequestHeader(value = "Accept", defaultValue = "") String acceptHeader) {

		String rdf;
		try {
			rdf = entityAPI.callDBPediaSpotlight(text);
		} catch (ExternalServiceFailedException e) {
			e.printStackTrace();
			return externalServiceFailedResponse();
		}

		// get output format
		RDFConstants.RDFSerialization outputFormat = getOutputSerialization(acceptHeader);
		try {
			Model model = rdfConversionService.unserializeRDF(rdf,
					RDFConstants.RDFSerialization.TURTLE);
			String serialization = rdfConversionService.serializeRDF(model,
					outputFormat);
			return new ResponseEntity<String>(serialization, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
