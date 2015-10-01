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
