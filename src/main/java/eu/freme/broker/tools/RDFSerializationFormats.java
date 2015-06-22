package eu.freme.broker.tools;

import java.util.HashMap;

import eu.freme.conversion.rdf.RDFConstants;

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
		put("application/json+ld",RDFConstants.RDFSerialization.JSON_LD);
		put("text", RDFConstants.RDFSerialization.PLAINTEXT);
		put("turtle", RDFConstants.RDFSerialization.TURTLE);
		put("json-ld", RDFConstants.RDFSerialization.JSON_LD);
		put("application/x-turtle", RDFConstants.RDFSerialization.TURTLE);
		put("application/rdf+xml", RDFConstants.RDFSerialization.RDF_XML);
		put("rdf-xml", RDFConstants.RDFSerialization.RDF_XML);
		put("application/n-triples", RDFConstants.RDFSerialization.N_TRIPLES);
		put("text/n3", RDFConstants.RDFSerialization.N3);
	}
}
