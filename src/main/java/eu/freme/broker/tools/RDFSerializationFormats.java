package eu.freme.broker.tools;

import java.util.HashMap;

import eu.freme.conversion.rdf.RDFConstants;

@SuppressWarnings("serial")
public class RDFSerializationFormats extends HashMap<String, RDFConstants.RDFSerialization>{

	public RDFSerializationFormats(){
		super();
		put("text/turtle", RDFConstants.RDFSerialization.TURTLE);
		put("application/json+ld",RDFConstants.RDFSerialization.JSON_LD);
		put("text", RDFConstants.RDFSerialization.PLAINTEXT);
		put("turtle", RDFConstants.RDFSerialization.TURTLE);
		put("json-ld", RDFConstants.RDFSerialization.JSON_LD);
	}
}
