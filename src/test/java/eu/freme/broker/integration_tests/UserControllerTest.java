package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDecisionVoter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.broker.eservices.BaseRestController;

public class UserControllerTest {

	String baseUrl = null;
	Logger logger = Logger.getLogger(UserControllerTest.class);

	@Value("${backend.admin.username}")
	String adminUsername;

	@Value("${backend.admin.password}")
	String adminPassword;

	@Before
	public void setup() {
		baseUrl = IntegrationTestSetup.getURLEndpoint();
	}

	@Test
	public void testUserSecurity() throws UnirestException {

		String username = "myuser";
		String password = "mypassword";

		logger.info("create user");
		HttpResponse<String> response = Unirest.post(baseUrl + "/user")
				.queryString("username", username)
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());
		String responseUsername = new JSONObject(response.getBody()).getString("name");
		assertTrue(username.equals(responseUsername));
		
		logger.info("create user with dublicate username - should not work, exception is ok");
		response = Unirest.post(baseUrl + "/user")
				.queryString("username", username)
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
		
		logger.info("create users with invalid usernames - should not work");
		response = Unirest.post(baseUrl + "/user")
				.queryString("username", "123")
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

		response = Unirest.post(baseUrl + "/user")
				.queryString("username", "*abc")
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());
		
		response = Unirest.post(baseUrl + "/user")
				.queryString("username", adminUsername)
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

		response = Unirest.post(baseUrl + "/user")
				.queryString("username", "")
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST.value());

		logger.info("create user with dublicate username - should not work, exception is ok");
		response = Unirest
				.post(baseUrl + BaseRestController.authenticationEndpoint)
				.header("X-Auth-Username", username)
				.header("X-Auth-Password", password + "xyz").asString();
		assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());

		logger.info("login with new user / create token");
		response = Unirest
				.post(baseUrl + BaseRestController.authenticationEndpoint)
				.header("X-Auth-Username", username)
				.header("X-Auth-Password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());
		String token = new JSONObject(response.getBody()).getString("token");
		assertTrue(token != null);
		assertTrue(token.length() > 0);

		// delete user without credentials should fail
		logger.info("delete user without providing credentials - should fail, exception is ok");
		response = Unirest.delete(baseUrl + "/user/" + username).asString();
		assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());

		// create another user
		logger.info("create a 2nd user");
		String otherUsername = "otheruser";
		response = Unirest.post(baseUrl + "/user").queryString("username", otherUsername)
				.queryString("password", password).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());
		String responseOtherUsername = new JSONObject(response.getBody()).getString("name");
		assertTrue( otherUsername.equals(responseOtherUsername));

		// delete other user should fail
		response = Unirest
				.delete(baseUrl + "/user/" + otherUsername)
				.header("X-Auth-Token", token).asString();
		assertTrue(response.getStatus() == HttpStatus.UNAUTHORIZED.value());
		
		// delete own user should work
		logger.info("delete own user - should work");
		response = Unirest
				.delete(baseUrl + "/user/" + username)
				.header("X-Auth-Token", token).asString();
		assertTrue(response.getStatus() == HttpStatus.NO_CONTENT.value());
	}
	
	@Test
	public void testAdmin(){
		
	}
}
