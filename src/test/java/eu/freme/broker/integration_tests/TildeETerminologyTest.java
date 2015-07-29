package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

/**
 * Test Tilde e-Terminology broker endpoint.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class TildeETerminologyTest extends IntegrationTest {


	public TildeETerminologyTest() {
		super("/e-terminology/tilde");
	}

	@Test
	public void testETerminology() throws Exception {

		String nif = readFile("src/test/resources/rdftest/e-terminology/example1.ttl");
		HttpResponse<String> response = baseRequest("")
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
