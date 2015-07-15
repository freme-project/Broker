package eu.freme.broker.tools;

import java.util.HashMap;

import eu.freme.common.conversion.rdf.RDFConstants;

@SuppressWarnings("serial")
public class RDFELinkSerializationFormats extends HashMap<String, RDFConstants.RDFSerialization>{

	public RDFELinkSerializationFormats(){
		super();
		put("text/turtle", RDFConstants.RDFSerialization.TURTLE);
		put("application/x-turtle", RDFConstants.RDFSerialization.TURTLE);
		put("turtle", RDFConstants.RDFSerialization.TURTLE);

                put("application/json+ld",RDFConstants.RDFSerialization.JSON_LD);
		put("json-ld", RDFConstants.RDFSerialization.JSON_LD);
		
                put("application/n-triples",RDFConstants.RDFSerialization.N_TRIPLES);
		put("ntriples", RDFConstants.RDFSerialization.N_TRIPLES);

		put("application/rdf+xml", RDFConstants.RDFSerialization.RDF_XML);
		put("rdf-xml", RDFConstants.RDFSerialization.RDF_XML);
                
                put("text/n3",RDFConstants.RDFSerialization.N3);
		put("n3",RDFConstants.RDFSerialization.N3);
                
		put("application/json", RDFConstants.RDFSerialization.JSON);
		put("json", RDFConstants.RDFSerialization.JSON);
	}
}
