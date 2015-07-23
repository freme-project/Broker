package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

/**
 * Test Tilde e-Terminology broker endpoint.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Ignore
public class TildeETerminologyTest {

	String url;

	@Before
	public void setup() {
		url = IntegrationTestSetup.getURLEndpoint() + "/e-terminology/tilde";
	}

	private String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));
		StringBuilder bldr = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			bldr.append(line);
			bldr.append("\n");
		}
		reader.close();
		return bldr.toString();
	}

	@Test
	public void testETerminology() throws Exception {

		String nif = readFile("src/test/resources/rdftest/e-terminology/example1.ttl");
		HttpResponse<String> response = Unirest.post(url)
				.queryString("source-lang", "en")
				.queryString("target-lang", "de")
				.queryString("informat", "turtle")
				.queryString("outformat", "turtle").body(nif).asString();

		assertTrue(response.getStatus() == HttpStatus.OK.value());

		// not working due to bug in tilde terminology api
		// String input =
		// response = Unirest.post(url)
		// .queryString("source-lang", "en")
		// .queryString("target-lang", "de")
		// .queryString("informat", "text")
		// .queryString("outformat", "turtle")
		// .body("hello world")
		// .asString();
		//
		// assertTrue(response.getStatus() == HttpStatus.OK.value());
	}
}
