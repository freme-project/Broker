package eu.freme.broker;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import scala.annotation.meta.setter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;

public class TildeETranslationTest {

	String baseUrl = "http://localhost:8080/";
	
	ConfigurableApplicationContext context;

	@Before
	public void setup() {
		context = SpringApplication.run(BrokerConfig.class);
	}

	@Test
	public void testPlaintextEtranslate() throws UnirestException {
		String clientId = "u-bd13faca-b816-4085-95d5-05373d695ab7";
		String sourceLang = "en";
		String targetLang = "de";
		String translationSystemId = "smt-76cd2e73-05c6-4d51-b02f-4fc9c4d40813";

		HttpResponse<String> response = Unirest
				.post(baseUrl
						+ "e-translate/tilde?input={input}&client-id={clientId}&source-lang={sourceLang}&target-lang={targetLang}&translation-system-id={translationSystemId}")
				.routeParam("input", "hello world")
				.header("Content-Type", "plaintext")
				.routeParam("clientId", clientId)
				.routeParam("sourceLang", sourceLang)
				.routeParam("targetLang", targetLang)
				.routeParam("translationSystemId", translationSystemId).asString();

		assertTrue(response.getStatus()==200);
	}

	private String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
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
	public void testTurtleETranslate() throws IOException, UnirestException{
		String data = readFile( "src/test/resources/rdftest/e-translate/data.turtle" );
		RequestBodyEntity request = createBaseRequest().header("Content-Type", "text/turtle").body(data);
		HttpResponse<String> response = request.asString();
		assertTrue(response.getStatus()==200);
		assertTrue(response.getBody().length()>0);
	}
	
	@Test
	public void testJsonLdETranslate() throws IOException, UnirestException{
		String data = readFile( "src/test/resources/rdftest/e-translate/data.json" );
		RequestBodyEntity request = createBaseRequest().header("Content-Type", "application/json+ld").body(data);
		HttpResponse<String> response = request.asString();
		assertTrue(response.getStatus()==200);
		assertTrue(response.getBody().length()>0);
	}

	/**
	 * Create basic get request to e-Translate
	 * 
	 * @return
	 */
	private HttpRequestWithBody createBaseRequest() {
		String clientId = "u-bd13faca-b816-4085-95d5-05373d695ab7";
		String sourceLang = "en";
		String targetLang = "de";
		String translationSystemId = "smt-76cd2e73-05c6-4d51-b02f-4fc9c4d40813";

		return Unirest
				.post(baseUrl
						+ "e-translate/tilde?client-id={clientId}&source-lang={sourceLang}&target-lang={targetLang}&translation-system-id={translationSystemId}")
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
