package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDecisionVoter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.broker.eservices.BaseRestController;

public class UserControllerTest {

	String baseUrl = null;

	@Before
	public void setup() {
		baseUrl = IntegrationTestSetup.getURLEndpoint();
	}

	@Test
	public void testSecurity() throws UnirestException {

		String username = "my-user";
		String password = "my-password";
		
		// create user
		HttpResponse<String> response = Unirest.post(baseUrl + "/user")
				.queryString("username", username)
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());
		System.err.println(response.getBody());

		// create dublicate username should not work
		response = Unirest.post(baseUrl + "/user")
				.queryString("username", username)
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

		// login with wrong password should fail
		response = Unirest
				.post(baseUrl + BaseRestController.authenticationEndpoint)
				.header("X-Auth-Username", username)
				.header("X-Auth-Password", password + "xyz").asString();
		assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());
		
		// login with new user
		response = Unirest
				.post(baseUrl + BaseRestController.authenticationEndpoint)
				.header("X-Auth-Username", username)
				.header("X-Auth-Password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());
		String token = new JSONObject(response.getBody()).getString("token");
		assertTrue(token != null);
		assertTrue(token.length() > 0);

		//
		// // normal non authenticated call should pass
		// HttpResponse<String> response = Unirest.post(baseUrl +
		// "/non-auth-call").asString();
		// assertTrue(response.getStatus() == HttpStatus.OK.value());
		//
		// // authenticated call with invalid token should not pass
		// response = Unirest.post(baseUrl +
		// "/non-auth-call").header("X-Auth-Token", "abcde").asString();
		// assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());
		//
		// // login with admin account
		// response = Unirest.post(baseUrl +
		// BaseRestController.authenticationEndpoint).
		// header("X-Auth-Username", "admin").
		// header("X-Auth-Password", "password").
		// asString();
		// assertTrue(response.getStatus() == HttpStatus.OK.value());
		// String token = new JSONObject(response.getBody()).getString("token");
		// System.err.println(token);

	}
}
