/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.broker.tools;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class to create a NIFParameterSet according to the specification of NIF.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class NIFParameterFactory {

	@Autowired
	RDFSerializationFormats rdfSerializationFormats;

	String defaultPrefix = "http://freme-project.eu/";

	public final Set<String> NIF_PARAMETERS = new HashSet<>(Arrays.asList(new String[]{
			"input", "i", "informat", "f", "outformat", "o", "prefix", "p"
	}));

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

	public boolean isNIFParameter(String parameter){
		return NIF_PARAMETERS.contains(parameter);
	}
}
