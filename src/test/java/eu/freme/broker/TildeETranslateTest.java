package eu.freme.broker;

import static org.junit.Assert.*;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class TildeETranslateTest {

	String baseUrl = "http://localhost:8080/";
	ConfigurableApplicationContext context;

	@Before
	public void setup() {
		context = SpringApplication.run(BrokerConfig.class);
	}
	
	@Test
	public void testEtranslate() throws UnirestException {
		
		String input = "hello world";
		String inputType = "plaintext";
		String clientId = "u-bd13faca-b816-4085-95d5-05373d695ab7";
		String sourceLang = "en";
		String targetLang = "de";
		String translationSystemId = "smt-76cd2e73-05c6-4d51-b02f-4fc9c4d40813";
	
		HttpResponse<String> response = Unirest.get(baseUrl + "e-translate/tilde?input={input}&input-type={inputType}&client-id={clientId}&source-lang={sourceLang}&target-lang={targetLang}&translation-system-id={translationSystemId}")
				.routeParam("input", input)
				.routeParam("inputType", inputType)
				.routeParam("clientId", clientId)
				.routeParam("sourceLang", sourceLang)
				.routeParam("targetLang", targetLang)
				.routeParam("translationSystemId", translationSystemId)
				.asString();
		
		assertTrue( response.getStatus() == HttpStatus.OK.value() );
		
		//TODO add more tests as soon as output is more elaborate
	}

	@After
	public void teardown() {
		context.close();
	}

}
