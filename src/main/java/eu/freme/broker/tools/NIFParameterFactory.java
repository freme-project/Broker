package eu.freme.broker.tools;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;

/**
 * Helper class to create a NIFParameterSet according to the specification of NIF.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class NIFParameterFactory {

	// used for swagger annotations
	public static final String nifFormatsString = "json-ld, turtle, n3, n-triples, rdf-xml";
	public static final String nifFormatsMimeString = "text/turtle, application/json+ld, application/n-triples, application/rdf+xml, text/n3";
	public static final String[] nifFormatsMime = {"text/turtle", "application/json+ld", "application/n-triples", "application/rdf+xml", "text/n3"};

	@Autowired
	RDFSerializationFormats rdfSerializationFormats;

	String defaultPrefix = "http://freme-project.eu/";

	public NIFParameterSet constructFromHttp(String input, String informat,
			String outformat, String postBody, String acceptHeader,
			String contentTypeHeader, String prefix) throws BadRequestException {

		String thisInput;
		if (input == null && postBody == null) {
			throw new BadRequestException("no input found in request");
		} else if (input != null) {
			thisInput = input;
		} else {
			thisInput = postBody;
		}

		RDFSerialization thisInformat;
		if (informat == null && contentTypeHeader == null) {
			thisInformat = RDFSerialization.TURTLE;
		} else if (informat != null) {
			if (!rdfSerializationFormats.containsKey(informat)) {
				throw new BadRequestException(
						"parameter informat has invalid value \"" + informat
								+ "\"");
			}
			thisInformat = rdfSerializationFormats.get(informat);
		} else {
			String[] contentTypeHeaderParts = contentTypeHeader.split(";");
			if (!rdfSerializationFormats.containsKey(contentTypeHeaderParts[0])) {
				throw new BadRequestException(
						"Content-Type header has invalid value \""
								+ contentTypeHeader + "\"");
			}
			thisInformat = rdfSerializationFormats.get(contentTypeHeaderParts[0]);
		}

		RDFSerialization thisOutformat;
		if( acceptHeader != null && acceptHeader.equals("*/*")){
			acceptHeader = "text/turtle";
		}
		if (outformat == null && acceptHeader == null) {
			thisOutformat = RDFSerialization.TURTLE;
		} else if (outformat != null) {
			if (!rdfSerializationFormats.containsKey(outformat)) {
				throw new BadRequestException(
						"Parameter outformat has invalid value \"" + outformat
								+ "\"");
			}
			thisOutformat = rdfSerializationFormats.get(outformat);
		} else {
			if (!rdfSerializationFormats.containsKey(acceptHeader)) {
				throw new BadRequestException(
						"Parameter outformat has invalid value \""
								+ acceptHeader + "\"");
			}
			thisOutformat = rdfSerializationFormats.get(acceptHeader);
		}

		String thisPrefix;
		if (prefix == null) {
			thisPrefix = defaultPrefix;
		} else{
			thisPrefix = prefix;
		}
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes);
		if (!urlValidator.isValid(thisPrefix)) {
			throw new BadRequestException("invalid prefix");
		}

		return new NIFParameterSet(thisInput, thisInformat, thisOutformat, thisPrefix);
	}
}
