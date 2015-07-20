package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class SecurityTest {

	String baseUrl = null;
	
	@Before
	public void setup(){
		baseUrl = IntegrationTestSetup.getURLEndpoint();
	}
	
	@Test
	public void testSecurity() throws UnirestException{
		
		HttpResponse<String> response = Unirest.post(baseUrl + "/security").asString();
		System.out.println(response.getStatus());
		System.out.println(response.getBody());
	}
}
