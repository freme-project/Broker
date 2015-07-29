package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;

import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.conversion.rdf.*;


public class TildeETranslationTest extends IntegrationTest{

	//String url = null;

	String sourceLang = "en";
	String targetLang = "de";

	public TildeETranslationTest(){super("/e-translation/tilde");}

	private HttpRequestWithBody baseRequest() {
		return baseRequest("")
				.queryString("source-lang", sourceLang)
				.queryString("target-lang", targetLang);
	}

	@Test
	public void testEtranslate() throws UnirestException, IOException, Exception {

		HttpResponse<String> response = baseRequest()
				.queryString("input", "hello world")
				.queryString("informat", "text").asString();
		assertTrue(response.getStatus() == 200);

		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE));

		String data = readFile("src/test/resources/rdftest/e-translate/data.turtle");
		response = baseRequest().header("Content-Type", "text/turtle")
				.body(data).asString();

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE));

		data = readFile("src/test/resources/rdftest/e-translate/data.json");
		response = baseRequest().header("Content-Type", "application/json+ld")
				.body(data).asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE));
		
		data = readFile("src/test/resources/rdftest/e-translate/data.txt");
		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text").asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE));
	}
}
