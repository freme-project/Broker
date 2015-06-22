package eu.freme.broker;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

/**
 * Test Tilde e-Translation broker endpoint.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class TildeETranslationTest {

	String url = "http://localhost:8080/e-translation/tilde";
	ConfigurableApplicationContext context;

	String clientId = "u-bd13faca-b816-4085-95d5-05373d695ab7";
	String sourceLang = "en";
	String targetLang = "de";
	String translationSystemId = "smt-76cd2e73-05c6-4d51-b02f-4fc9c4d40813";

	@Before
	public void setup() {
		context = SpringApplication.run(FremeFullConfig.class);
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
		return Unirest.post(url).queryString("client-id", clientId)
				.queryString("source-lang", sourceLang)
				.queryString("target-lang", targetLang)
				.queryString("translation-system-id", translationSystemId);
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
		response = baseRequest().header("Content-Type",
				"application/json+ld").body(data).asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		
		data = readFile("src/test/resources/rdftest/e-translate/data.txt");
		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text").asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}

	@After
	public void teardown() {
		context.close();
	}

}
