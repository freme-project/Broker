/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
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

import java.util.HashMap;

import eu.freme.conversion.rdf.RDFConstants;

/**
 * Defines the RDFSerializationFormats accepted by the REST endpoints.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SuppressWarnings("serial")
public class RDFSerializationFormats extends
		HashMap<String, RDFConstants.RDFSerialization> {
	
	public RDFSerializationFormats() {
		super();
		put("text/turtle", RDFConstants.RDFSerialization.TURTLE);
		put("application/x-turtle", RDFConstants.RDFSerialization.TURTLE);
		put("turtle", RDFConstants.RDFSerialization.TURTLE);

		put("application/json+ld", RDFConstants.RDFSerialization.JSON_LD);
		put("application/ld+json", RDFConstants.RDFSerialization.JSON_LD);
		put("json-ld", RDFConstants.RDFSerialization.JSON_LD);

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
