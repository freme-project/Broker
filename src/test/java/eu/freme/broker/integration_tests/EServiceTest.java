/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.broker.integration_tests;

import com.hp.hpl.jena.shared.AssertionFailureException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.broker.eservices.BaseRestController;
import eu.freme.broker.tools.ratelimiter.RateLimitingFilter;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.filter.ExpressionFilter;
import org.json.JSONObject;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by Arne on 29.07.2015.
 */

public abstract class EServiceTest {

    private String url = null;
    private String baseUrl = null;
    private String service;
    public RDFConversionService converter;
    public Logger logger = Logger.getLogger(EServiceTest.class);


    public static String tokenWithPermission;
    public static String tokenWithOutPermission;
    public static String tokenAdmin;

    public static final String accessDeniedExceptions = "eu.freme.broker.exception.AccessDeniedException || EXCEPTION ~=org.springframework.security.access.AccessDeniedException";

    protected final String usernameWithPermission = "userwithpermission";
    protected final String passwordWithPermission = "testpassword";
    protected final String usernameWithoutPermission = "userwithoutpermission";
    protected final String passwordWithoutPermission = "testpassword";

    private boolean authenticate = false;
    private static boolean authenticated = false;

    public EServiceTest(String service){
        this.service = service;
        Unirest.setTimeouts(10000, 180000);
    }

    @Before
    public void setup() throws UnirestException {
        baseUrl = IntegrationTestSetup.getURLEndpoint();
        url=baseUrl+service;
        converter = (RDFConversionService)IntegrationTestSetup.getContext().getBean(RDFConversionService.class);
        if(!authenticated && authenticate)
            authenticateUsers();
    }

    private void authenticateUsers() throws UnirestException {

        //Creates two users, one intended to have permission, the other not
        createUser(usernameWithPermission, passwordWithPermission);
        tokenWithPermission = authenticateUser(usernameWithPermission, passwordWithPermission);
        createUser(usernameWithoutPermission, passwordWithoutPermission);
        tokenWithOutPermission = authenticateUser(usernameWithoutPermission, passwordWithoutPermission);
        ConfigurableApplicationContext context = IntegrationTestSetup.getApplicationContext();
        tokenAdmin = authenticateUser(context.getEnvironment().getProperty("admin.username"), context.getEnvironment().getProperty("admin.password"));
        authenticated = true;
    }

    //All HTTP Methods used in FREME are defined.
    protected HttpRequestWithBody baseRequestPost(String function) {
        return Unirest.post(url + function);
    }
    protected HttpRequest baseRequestGet( String function) {return Unirest.get(url + function);}
    protected HttpRequestWithBody baseRequestDelete( String function) {
        return Unirest.delete(url + function);
    }
    protected HttpRequestWithBody baseRequestPut( String function) {
        return Unirest.put(url + function);
    }

    protected HttpRequestWithBody baseRequestPost(String function, String token) {
        if(token!=null)
            return Unirest.post(url + function).header("X-Auth-Token", token);
        return Unirest.post(url + function);
    }
    protected HttpRequest baseRequestGet( String function, String token) {
        if(token!=null)
            return Unirest.get(url + function).header("X-Auth-Token", token);
        return Unirest.get(url + function);
    }
    protected HttpRequestWithBody baseRequestDelete( String function, String token) {
        if(token!=null)
            return Unirest.delete(url + function).header("X-Auth-Token", token);
        return Unirest.delete(url + function);
    }
    protected HttpRequestWithBody baseRequestPut( String function, String token) {
        if(token!=null)
            return Unirest.put(url + function).header("X-Auth-Token", token);
        return Unirest.put(url + function);
    }

    //Simple getter which returns the url to the endpoint
    public String getUrl() {
        return url;
    }
    public String getBaseUrl() {
        return baseUrl;
    }


    //Reads a text file line by line. Use this when testing API with examples from /test/resources/
    public static String readFile(String file) throws IOException {
        StringBuilder bldr = new StringBuilder();
        for (String line: Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8)) {
            bldr.append(line).append('\n');
        }
        return bldr.toString();
    }

    //General NIF Validation: can be used to test all NiF Responses on their validity.
    public void validateNIFResponse(HttpResponse<String> response, RDFConstants.RDFSerialization nifformat) throws IOException {

        //basic tests on response
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getBody().length() > 0);
        assertTrue(!response.getHeaders().isEmpty());
        assertNotNull(response.getHeaders().get("content-type"));

        //Tests if headers are correct.
        String contentType = response.getHeaders().get("content-type").get(0).split(";")[0];
        //TODO: Special Case due to WRONG Mime Type application/json+ld.
        // We wait for https://github.com/freme-project/technical-discussion/issues/40
        if (contentType.equals("application/ld+json") && nifformat.contentType().equals("application/json+ld")) {
        } else {
            assertEquals(contentType, nifformat.contentType());
        }

        if(nifformat!= RDFConstants.RDFSerialization.JSON) {
            // validate RDF
            try {
                assertNotNull(converter.unserializeRDF(response.getBody(), nifformat));
            } catch (Exception e) {
                throw new AssertionFailureException("RDF validation failed");
            }
        }



        // validate NIF
        /* the Validate modul is available just as SNAPSHOT.
        if (nifformat == RDFConstants.RDFSerialization.TURTLE) {
            Validate.main(new String[]{"-i", response.getBody(), "--informat","turtle"});
        } else if (nifformat == RDFConstants.RDFSerialization.RDF_XML) {
            Validate.main(new String[]{"-i", response.getBody(), "--informat","rdfxml"});
        } else {
            //Not implemented yet: n3, n-triples, json-ld
            Validate.main(new String[]{"-i", response.getBody()});
        }
        */

    }



    public void enableAuthenticate() {
        authenticate = true;
    }

    public void createUser(String username, String password) throws UnirestException {

        HttpResponse<String> response = Unirest.post(getBaseUrl() + "/user")
                .queryString("username", username)
                .queryString("password", password).asString();
        logger.debug("STATUS: " + response.getStatus());
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

    public String authenticateUser(String username, String password) throws UnirestException{
        HttpResponse<String> response;

        logger.info("login with new user / create token");
        response = Unirest
                .post(getBaseUrl()  + BaseRestController.authenticationEndpoint)
                .header("X-Auth-Username", username)
                .header("X-Auth-Password", password).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
        String token = new JSONObject(response.getBody()).getString("token");
        return token;
    }

    public void loggerIgnore(Class x){
        loggerIgnore(x.getName());
    }
    public void loggerIgnore(String x) {

        String newExpression="EXCEPTION ~="+x;
        Appender appender=(Appender)Logger.getRootLogger().getAllAppenders().nextElement();

        String oldExpression;
        ExpressionFilter exp;
        try {
            exp= ((ExpressionFilter) appender.getFilter());
            oldExpression=exp.getExpression();
            if (!oldExpression.contains(newExpression)) {
                exp.setExpression(oldExpression+" || " + newExpression);
                exp.activateOptions();
            }
        } catch (NullPointerException e) {
            exp= new ExpressionFilter();
            exp.setExpression(newExpression);
            exp.setAcceptOnMatch(false);
            exp.activateOptions();
            appender.clearFilters();
            appender.addFilter(exp);
        }
    }

    public void loggerUnignore(Class x) {
        loggerUnignore(x.getName());
    }

    public void loggerUnignore(String x) {

        Appender appender=(Appender)Logger.getRootLogger().getAllAppenders().nextElement();

        ExpressionFilter exp= ((ExpressionFilter) appender.getFilter());
        exp.setExpression(exp.getExpression().replace("|| EXCEPTION ~="+x,"").replace("EXCEPTION ~="+x+ "||",""));
        exp.activateOptions();
        appender.clearFilters();
        appender.addFilter(exp);

    }

}
