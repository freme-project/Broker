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
package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;

import org.junit.Ignore;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.conversion.rdf.RDFConstants;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Ignore
public class TildeETranslationTest extends IntegrationTest{

	String sourceLang = "en";
	String targetLang = "de";

	public TildeETranslationTest(){super("/e-translation/tilde");}

	private HttpRequestWithBody baseRequest() {
		return baseRequestPost("")
				.queryString("source-lang", sourceLang)
				.queryString("target-lang", targetLang);
	}

	@Test
	public void testEtranslate() throws UnirestException, IOException, Exception {

		HttpResponse<String> response = baseRequest()
				.queryString("informat", "text")
				.queryString("input", "hello world")
				.queryString("outformat","rdf-xml")
				.asString();
		
		validateNIFResponse(response, RDFConstants.RDFSerialization.RDF_XML);


		String data = readFile("src/test/resources/rdftest/e-translate/data.turtle");
		response = baseRequest().header("Content-Type", "text/turtle")
				.body(data).asString();
		validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);

		data = readFile("src/test/resources/rdftest/e-translate/data.json");
		response = baseRequest().header("Content-Type", "application/json+ld")
				.queryString("outformat","json-ld")
				.body(data).asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);
		
		data = readFile("src/test/resources/rdftest/e-translate/data.txt");
		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text")
				.queryString("outformat","n3")
				.asString();
		validateNIFResponse(response, RDFConstants.RDFSerialization.N3);


		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text")
				.queryString("outformat","n-triples")
				.asString();
		validateNIFResponse(response, RDFConstants.RDFSerialization.N_TRIPLES);
	}
}
