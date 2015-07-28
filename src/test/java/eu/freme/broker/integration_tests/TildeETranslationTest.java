package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

public class TildeETranslationTest {

	String url = null;

	String sourceLang = "en";
	String targetLang = "de";
	@Before
	public void setup(){
		url = IntegrationTestSetup.getURLEndpoint() + "/e-translation/tilde";
	}

	private String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		StringBuilder bldr = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			bldr.append(line);
			bldr.append("\n");
		}
		reader.close();
		return bldr.toString();
	}

	private HttpRequestWithBody baseRequest() {
		return Unirest.post(url)
				.queryString("source-lang", sourceLang)
				.queryString("target-lang", targetLang);
	}

	@Test
	public void testEtranslate() throws UnirestException, IOException {

		HttpResponse<String> response = baseRequest()
				.queryString("input", "hello world")
				.queryString("informat", "text").asString();
		assertTrue(response.getStatus() == 200);

		String data = readFile("src/test/resources/rdftest/e-translate/data.turtle");
		response = baseRequest().header("Content-Type", "text/turtle")
				.body(data).asString();

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);

		data = readFile("src/test/resources/rdftest/e-translate/data.json");
		response = baseRequest().header("Content-Type", "application/json+ld")
				.body(data).asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		
		data = readFile("src/test/resources/rdftest/e-translate/data.txt");
		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text").asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}
}
