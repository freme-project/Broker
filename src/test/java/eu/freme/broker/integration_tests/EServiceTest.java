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
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.filter.ExpressionFilter;
import org.json.JSONObject;
import org.junit.Before;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

import static org.junit.Assert.*;

/**
 * This class should simplify the testing of an e-service.
 * After instantiation for a certain service, it provides post/get/put/delete request functionality
 * via corresponding methods, with requested url = baseUrl + service + function.
 * If enabled via anableAuthentication() two authenticated users,
 * "userwithpermission" and "userwithoutpermission", are provided
 * to test security issues or api endpoints, which rely on authenticated access.
 * Use addAuthentication() [optionally with getTokenWithoutPermission()] in these cases.
 * For additional authentication purposes users can be created and authenticated
 * via createUser() and authenticateUser().
 * Furthermore a RDF response can be validated with validateNIFResponse().
 * The logging mechanism can be manipulated to avoid printing of full exception
 * stack traces for expected exceptions, see loggerIgnore() and below.
 */
public abstract class EServiceTest {

    // Url of the e-Service: baseUrl + service
    private String serviceUrl;
    // The baseUrl is defined by the config parameter freme.test.baseurl
    // or the Spring Boot application context and consists of PROTOCOL://HOST:PORT
    private String baseUrl;
    private String service;
    public RDFConversionService converter;
    public Logger logger = Logger.getLogger(EServiceTest.class);


    private static String tokenWithPermission;
    private static String tokenWithOutPermission;
    private static String tokenAdmin;

    public static final String accessDeniedExceptions = "eu.freme.broker.exception.AccessDeniedException || EXCEPTION ~=org.springframework.security.access.AccessDeniedException";

    protected final String usernameWithPermission = "userwithpermission";
    protected final String passwordWithPermission = "testpassword";
    protected final String usernameWithoutPermission = "userwithoutpermission";
    protected final String passwordWithoutPermission = "testpassword";

    private boolean authenticate = false;
    private static boolean authenticated = false;

    /**
     * To instantiate an EServiceTest the service has to be set.
     * This is needed to construct the full endpoint URL = baseUrl + service + function
     * when using get/post/put/delete methods or to use getServiceUrl.
     * The baseUrl is defined by the config parameter freme.test.baseurl
     * or the Spring Boot application context and consists of PROTOCOL://HOST:PORT
     * @param service The service to test with this instantiation
     */
    public EServiceTest(String service){
        this.service = service;
        Unirest.setTimeouts(10000, 180000);
    }

    @Before
    public void setup() throws UnirestException {
        baseUrl = IntegrationTestSetup.getURLEndpoint();
        serviceUrl = baseUrl+service;
        converter = (RDFConversionService)IntegrationTestSetup.getContext().getBean(RDFConversionService.class);
        if(!authenticated && authenticate)
            authenticateUsers();
    }

    /**
     * This method creates and authenticats two users, userwithpermission and userwithoutpermission.
     * Furthermore the admin token is created.
     * @throws UnirestException
     */
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

    // All HTTP Methods used in FREME are defined.
    // Overwrite them in inherited classe to add certain parameters to all requests of a kind, if needed.
    protected HttpRequestWithBody post(String function) {return Unirest.post(serviceUrl + function);}
    protected HttpRequest get(String function) {return Unirest.get(serviceUrl + function);}
    protected HttpRequestWithBody delete(String function) {return Unirest.delete(serviceUrl + function);}
    protected HttpRequestWithBody put(String function) {return Unirest.put(serviceUrl + function);}

    public String getTokenWithPermission() {
        return tokenWithPermission;
    }

    public String getTokenWithOutPermission() {
        return tokenWithOutPermission;
    }

    public String getTokenAdmin() {
        return tokenAdmin;
    }

    /**
     * Use this method to add an authentication header to the request.
     * If the given token is null, the request will not be modified.
     * @param request The request to add the authentication
     * @param token The authentication Token
     * @param <T>
     * @return The modified request
     */
    @SuppressWarnings("unchecked")
    protected <T extends HttpRequest> T addAuthentication(T request, String token){
        if(token==null)
            return request;
        return (T)request.header("X-Auth-Token", token);
    }

    protected <T extends HttpRequest> T addAuthentication(T request){
        return addAuthentication(request, getTokenWithPermission());
    }

    //Simple getter which returns the Url of the e-Service
    public String getServiceUrl() {
        return serviceUrl;
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
        assertEquals(contentType, nifformat.contentType());

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

    /**
     * Enable authentication, if needed. This triggers the creation and authentication of the
     * users "userwithpermission" and "userwithoutpermission" and generates the corresponding
     * security tokens. Use getTokenWithPermission and getTokenWithOutPermission to authenticate the api calls
     * or to test security issues of the e-service.
     */
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


    /**
     * Sets specific LoggingFilters and initiates suppression of specified Exceptions in Log4j.
     * @param x Class of Exception
     **/
    public void loggerIgnore(Class<Throwable> x){
        loggerIgnore(x.getName());
    }

    /**
     * Sets specific LoggingFilters and initiates suppression of specified Exceptions in Log4j.
     * @param x String name of Exception
     **/
    public void loggerIgnore(String x) {

        String newExpression="EXCEPTION ~="+x;
        Enumeration<Appender> allAppenders= Logger.getRootLogger().getAllAppenders();
        Appender appender;

        while (allAppenders.hasMoreElements()) {
            appender=allAppenders.nextElement();
            String oldExpression;
            ExpressionFilter exp;
            try {
                exp = ((ExpressionFilter) appender.getFilter());
                oldExpression = exp.getExpression();
                if (!oldExpression.contains(newExpression)) {
                    exp.setExpression(oldExpression + " || " + newExpression);
                    exp.activateOptions();
                }
            } catch (NullPointerException e) {
                exp = new ExpressionFilter();
                exp.setExpression(newExpression);
                exp.setAcceptOnMatch(false);
                exp.activateOptions();
                appender.clearFilters();
                appender.addFilter(exp);
            }
        }
    }

    /**
     * Clears specific LoggingFilters and stops their suppression of Exceptions in Log4j.
     * @param x Class of Exception
     **/
    public void loggerUnignore(Class<Throwable> x) {
        loggerUnignore(x.getName());
    }

    /**
     * Clears specific LoggingFilters and stops their suppression of Exceptions in Log4j.
     * @param x String name of Exception
     **/
    public void loggerUnignore(String x) {
        Enumeration<Appender> allAppenders= Logger.getRootLogger().getAllAppenders();
        Appender appender;

        while (allAppenders.hasMoreElements()) {
            appender=allAppenders.nextElement();
            ExpressionFilter exp = ((ExpressionFilter) appender.getFilter());
            exp.setExpression(exp.getExpression().replace("|| EXCEPTION ~=" + x, "").replace("EXCEPTION ~=" + x + "||", ""));
            exp.activateOptions();
            appender.clearFilters();
            appender.addFilter(exp);
        }
    }

    /**
     * Clears all LoggingFilters for all Appenders. Stops suppression of Exceptions in Log4j.
     **/
    public void loggerClearFilters() {
        Enumeration<Appender> allAppenders = Logger.getRootLogger().getAllAppenders();
        Appender appender;

        while (allAppenders.hasMoreElements()) {
            appender = allAppenders.nextElement();
            appender.clearFilters();
        }
    }
}
