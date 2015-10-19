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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.common.conversion.rdf.RDFConstants;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 06.08.15.
 */
public class EInternationalizationTest extends EServiceTest {

	// Tests E-Internationalization Service used for converting html and xliff
	// to nif format
	// against POST /e-entity/freme-ner/documents
	public EInternationalizationTest() {
		super("/e-entity/freme-ner/documents");
	}

	String dataset = "dbpedia";
	String[] sample_xliff = { "test1.xlf" };
	String[] sample_html = { "aa324.html", "test10.html", "test12.html" };
	String resourcepath = "src/test/resources/e-internationalization/";

	@Test
	public void TestEInternationalization() throws IOException,
			UnirestException {
		// See EInternationalizationFilter
		// for (String sample_file : sample_xliff) {
		// testContentTypeandInformat("application/x-xliff+xml",readFile(resourcepath+sample_file));
		// }
		for (String sample_file : sample_html) {
			testContentTypeandInformat("text/html", readFile(resourcepath
					+ sample_file));
		}
	}

	protected HttpRequestWithBody baseRequestPost() {
		return super.baseRequestPost("").queryString("dataset", dataset);
	}

	private void testContentTypeandInformat(String format, String data)
			throws UnirestException, IOException {
		HttpResponse<String> response;
		// With Content-Type header
		response = baseRequestPost().header("Content-Type", format)
				.queryString("language", "en").body(data).asString();

		validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

		// With informat QueryString
		response = baseRequestPost().queryString("informat", format)
				.queryString("language", "en").body(data).asString();
		validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
	}

	@Test
	public void testRoundTripping() throws UnirestException, IOException {
		HttpResponse<String> response = Unirest
				.post(super.getBaseUrl() + "/e-entity/freme-ner/documents")
				.queryString("language", "en")
				.queryString("dataset", "dbpedia")
				.queryString("informat", "text/html")
				.queryString("outformat", "text/html")
				.body("<p>Berlin is a city in Germany</p>").asString();

		assertEquals(response.getStatus(), 200);
		assertTrue(response.getBody().length() > 0);

		String xliff = FileUtils.readFileToString(new File(
				"src/test/resources/e-internationalization/test1.xlf"));
		response = Unirest
				.post(super.getBaseUrl() + "/e-entity/freme-ner/documents")
				.queryString("language", "en")
				.queryString("dataset", "dbpedia")
				.queryString("informat", "text/html")
				.queryString("outformat", "text/html").body(xliff).asString();

		assertEquals(response.getStatus(), 200);
		assertTrue(response.getBody().length() > 0);
	}

	@Test
	public void testXml() throws UnirestException {
		HttpResponse<String> response = Unirest
				.post(super.getBaseUrl() + "/e-entity/freme-ner/documents")
				.queryString("language", "en")
				.queryString("dataset", "dbpedia")
				.queryString("informat", "text/xml")
				.body("<note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>")
				.asString();

		assertEquals(response.getStatus(), 200);
		assertTrue(response.getBody().length() > 0);
	}
	
	@Test
	public void testOdt() throws IOException, UnirestException{
		
		File file = new File("src/test/resources/e-internationalization/odt-test.odt");
		byte[] data = FileUtils.readFileToByteArray(file);
		
		HttpResponse<String> response = Unirest
				.post(super.getBaseUrl() + "/e-entity/freme-ner/documents")
				.queryString("language", "en")
				.queryString("dataset", "dbpedia")
				.queryString("informat", "application/x-openoffice")
				.body(data)
				.asString();

		assertEquals(response.getStatus(), 200);
		assertTrue(response.getBody().length() > 0);
	}

}
