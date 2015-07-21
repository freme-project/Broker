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
		Long userid = new JSONObject(response.getBody()).getLong("id");

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

		// delete user without credentials should fail
		System.err.println(baseUrl + "/user/ " + userid.toString());
		response = Unirest.delete(baseUrl + "/user/" + userid.toString()).asString();
		System.err.println(response.getStatus());
		System.err.println(response.getBody());
		assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());

//		// create another user
//		response = Unirest.post(baseUrl + "/user").queryString("username", "other-user")
//				.queryString("password", password).asString();
//		assertTrue(response.getStatus() == HttpStatus.OK.value());
//		Long otherUserid = new JSONObject(response.getBody()).getLong("id");
//
//		// delete other user should fail
//		response = Unirest
//				.delete(baseUrl + "/user/ " + otherUserid)
//				.header("X-Auth-Token", token).asString();
		

	}
}
