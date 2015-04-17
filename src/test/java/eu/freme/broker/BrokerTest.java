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

public class BrokerTest {

	String baseUrl = "http://localhost:8080/";
	ConfigurableApplicationContext context;

	@Before
	public void setup() {
		context = SpringApplication.run(BrokerConfig.class);
	}
	
	@Test
	public void testEtranslate() throws UnirestException {
		HttpResponse<String> response = Unirest.get(baseUrl + "e-translate/tilde?input={input}")
				.routeParam("input", "test").asString();
		
		assertTrue( response.getStatus() == HttpStatus.OK.value() );
		assertTrue( response.getBody().equals("TEST"));
	}

	@After
	public void teardown() {
		context.close();
	}

}
