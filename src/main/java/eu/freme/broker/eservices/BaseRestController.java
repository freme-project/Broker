package eu.freme.broker.eservices;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.freme.conversion.rdf.RDFConversionService;

public class BaseRestController {

	// see https://jena.apache.org/documentation/io/rdf-input.html for a list of http accept headers
	private HashMap<String,RDFConversionService.RDFSerialization> rdfFormats;
	private final RDFConversionService.RDFSerialization defaultRDFOutputFormat = RDFConversionService.RDFSerialization.JSON_LD;
	
	public BaseRestController(){
		rdfFormats = new HashMap<String, RDFConversionService.RDFSerialization>();
		rdfFormats.put("text/turtle", RDFConversionService.RDFSerialization.TURTLE);
		rdfFormats.put("application/ld+json", RDFConversionService.RDFSerialization.JSON_LD);
	}
		
	/**
	 * Create this response in the REST controller when the external service is unavailable or has reported an error.
	 * 
	 * @return
	 */
	protected ResponseEntity<String> externalServiceFailedResponse(){
		return new ResponseEntity<String>("The external service failed", HttpStatus.BAD_GATEWAY);
	}
	
	protected RDFConversionService.RDFSerialization getOutputSerialization( String acceptHeader ){
		
		if( rdfFormats.containsKey( acceptHeader )){
			return rdfFormats.get(acceptHeader);
		} else{
			return defaultRDFOutputFormat;
		}
	}
}
