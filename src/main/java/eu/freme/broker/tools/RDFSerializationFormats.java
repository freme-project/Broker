package eu.freme.broker.tools;

import java.util.HashMap;

import eu.freme.common.conversion.rdf.RDFConstants;

/**
 * Defines the RDFSerializationFormats accepted by the REST endpoints.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SuppressWarnings("serial")
public class RDFSerializationFormats extends HashMap<String, RDFConstants.RDFSerialization>{

	public RDFSerializationFormats(){
		super();
		put("text/turtle", RDFConstants.RDFSerialization.TURTLE);
		put("application/x-turtle", RDFConstants.RDFSerialization.TURTLE);
		put("turtle", RDFConstants.RDFSerialization.TURTLE);
                
		put("application/json+ld",RDFConstants.RDFSerialization.JSON_LD);
		put("json-ld",RDFConstants.RDFSerialization.JSON_LD);
                
		put("application/n-triples", RDFConstants.RDFSerialization.N_TRIPLES);
		put("n-triples", RDFConstants.RDFSerialization.N_TRIPLES);
                
		put("text/plain", RDFConstants.RDFSerialization.PLAINTEXT);
		put("text", RDFConstants.RDFSerialization.PLAINTEXT);
                
		put("application/rdf+xml", RDFConstants.RDFSerialization.RDF_XML);
		put("rdf-xml", RDFConstants.RDFSerialization.RDF_XML);
                
		put("text/n3", RDFConstants.RDFSerialization.N3);
		put("n3", RDFConstants.RDFSerialization.N3);
	}
}
