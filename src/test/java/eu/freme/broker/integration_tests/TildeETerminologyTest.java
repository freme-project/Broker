package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertTrue;

/**
 * Test Tilde e-Terminology broker endpoint.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Ignore //TODO: wait for issue:  Timeouts on e-Terminology #43 https://github.com/freme-project/Broker/issues/43
public class TildeETerminologyTest extends IntegrationTest {

	public TildeETerminologyTest() {
		super("/e-terminology/tilde");
	}

	@Test
	public void testETerminology() throws Exception {

		String nif = readFile("src/test/resources/rdftest/e-terminology/example1.ttl");
		HttpResponse<String> response = baseRequestPost("")
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
