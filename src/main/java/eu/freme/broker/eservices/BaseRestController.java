package eu.freme.broker.eservices;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hp.hpl.jena.rdf.model.Model;

import eu.freme.broker.exception.InvalidContentTypeException;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;

public class BaseRestController {

	// see https://jena.apache.org/documentation/io/rdf-input.html for a list of
	// http accept headers
	private HashMap<String, RDFConstants.RDFSerialization> rdfFormats;

	private final RDFConstants.RDFSerialization defaultRDFOutputFormat = RDFConstants.RDFSerialization.JSON_LD;

	final String inputTypePlaintext = "plaintext";

	Logger logger = Logger.getLogger(BaseRestController.class);

	@Autowired
	RDFConversionService rdfConversionService;

	public BaseRestController() {
		rdfFormats = new HashMap<String, RDFConstants.RDFSerialization>();
		rdfFormats.put("text/turtle", RDFConstants.RDFSerialization.TURTLE);
		rdfFormats.put("application/json+ld",
				RDFConstants.RDFSerialization.JSON_LD);
	}

	/**
	 * Create this response in the REST controller when the external service is
	 * unavailable or has reported an error.
	 * 
	 * @return
	 */
	protected ResponseEntity<String> externalServiceFailedResponse() {
		return new ResponseEntity<String>("The external service failed",
				HttpStatus.BAD_GATEWAY);
	}

	/**
	 * Return the rdf serialization format that user asked for with ACCEPT
	 * header. Invalid accept headers will be converted to
	 * defaultRDFOutputFormat (JSON-LD)
	 * 
	 * @param acceptHeader
	 * @return
	 */
	protected RDFConstants.RDFSerialization getOutputSerialization(
			String acceptHeader) {

		if (rdfFormats.containsKey(acceptHeader)) {
			return rdfFormats.get(acceptHeader);
		} else {
			return defaultRDFOutputFormat;
		}
	}

	/**
	 * Unserialize rdf to jena model
	 * 
	 * @param rdf
	 * @param inputFormat
	 * @return
	 * @throws Exception
	 */
	protected Model unserializeNIF(String rdf, String inputFormat)
			throws Exception {
		RDFConstants.RDFSerialization format = rdfFormats.get(inputFormat);
		if (format == null) {
			throw new RuntimeException("Unknown format: " + inputFormat);
		}
		return rdfConversionService.unserializeRDF(rdf, format);
	}

	/**
	 * Validate that Content-Type header has one of the valid values.
	 * 
	 * @param inputType
	 * @return
	 */
	protected boolean validateContentType(String inputType) {
		if( inputType.equals(inputTypePlaintext)
				|| rdfFormats.containsKey(inputType) ){
			return true;
		} else{
			throw new InvalidContentTypeException();
		}
	}
}
