package eu.freme.broker.eservices;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.rdf.model.Model;

import eu.freme.broker.tools.NIFParameterFactory;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.broker.tools.RDFELinkSerializationFormats;
import eu.freme.broker.tools.RDFSerializationFormats;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;

public class BaseRestController {


	Logger logger = Logger.getLogger(BaseRestController.class);

	@Autowired
	RDFConversionService rdfConversionService;
	
	@Autowired
	NIFParameterFactory nifParameterFactory;
	
	@Autowired
	RDFSerializationFormats rdfSerializationFormats;

	@Autowired
	RDFELinkSerializationFormats rdfELinkSerializationFormats;
        
	protected NIFParameterSet normalizeNif(String input, String informat,
			String outformat, String postBody, String acceptHeader,
			String contentTypeHeader, String prefix){
		return nifParameterFactory.constructFromHttp(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
	}
	
	protected String serializeNif(Model model, RDFConstants.RDFSerialization format) throws Exception{
		return rdfConversionService.serializeRDF(model, format);
	}
	
	protected Model unserializeNif(String nif, RDFConstants.RDFSerialization format) throws Exception{
		return rdfConversionService.unserializeRDF(nif, format);
	}

}
