package eu.freme.broker;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class TildeETranslationTest {

	String baseUrl = "http://localhost:8080/";
	ConfigurableApplicationContext context;

	@Before
	public void setup() {
		context = SpringApplication.run(BrokerConfig.class);
	}

	@Test
	public void testEtranslate() throws UnirestException {

		assertTrue(createBaseRequest().asString().getStatus() == HttpStatus.OK
				.value());

		// TODO add NIF validator here
	}

	/**
	 * Create basic get request to e-Translate
	 * 
	 * @return
	 */
	private HttpRequestWithBody createBaseRequest() {
		String input = "hello world";
		String inputType = "plaintext";
		String clientId = "u-bd13faca-b816-4085-95d5-05373d695ab7";
		String sourceLang = "en";
		String targetLang = "de";
		String translationSystemId = "smt-76cd2e73-05c6-4d51-b02f-4fc9c4d40813";

		return Unirest
				.post(baseUrl
						+ "e-translate/tilde?input={input}&input-type={inputType}&client-id={clientId}&source-lang={sourceLang}&target-lang={targetLang}&translation-system-id={translationSystemId}")
				.routeParam("input", input).routeParam("inputType", inputType)
				.routeParam("clientId", clientId)
				.routeParam("sourceLang", sourceLang)
				.routeParam("targetLang", targetLang)
				.routeParam("translationSystemId", translationSystemId);
	}

	@After
	public void teardown() {
		context.close();
	}

}
