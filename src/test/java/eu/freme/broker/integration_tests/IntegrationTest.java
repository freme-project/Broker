package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.eservices.BaseRestController;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Before;

import com.hp.hpl.jena.shared.AssertionFailureException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;
import org.springframework.http.HttpStatus;

/**
 * Created by Arne on 29.07.2015.
 */
public abstract class IntegrationTest {

    private String url = null;
    private String baseUrl = null;
    private String service;
    public RDFConversionService converter;
    public Logger logger = Logger.getLogger(IntegrationTest.class);


    public IntegrationTest(String service){
        this.service = service;
    }

    @Before
    public void setup(){
        baseUrl = IntegrationTestSetup.getURLEndpoint();
        url=baseUrl+service;
        converter = (RDFConversionService)IntegrationTestSetup.getContext().getBean(RDFConversionService.class);
    }

    //All HTTP Methods used in FREME are defined.
    protected HttpRequestWithBody baseRequestPost(String function) {
        return Unirest.post(url + function);
    }
    protected HttpRequest baseRequestGet( String function) {
        return Unirest.get(url + function);
    }
    protected HttpRequestWithBody baseRequestDelete( String function) {
        return Unirest.delete(url + function);
    }
    protected HttpRequestWithBody baseRequestPut( String function) {
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
        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);
        assertTrue(!response.getHeaders().isEmpty());
        assertNotNull(response.getHeaders().get("content-type"));

        //Tests if headers are correct.
        String contentType = response.getHeaders().get("content-type").get(0).split(";")[0];
        //TODO: Special Case due to WRONG Mime Type application/json+ld.
        // We wait for https://github.com/freme-project/technical-discussion/issues/40
        if (contentType.equals("application/ld+json") && nifformat.contentType().equals("application/json+ld")) {
        } else {
            assertTrue(contentType.equals(nifformat.contentType()));
        }

        // validate RDF
        try {
            assertNotNull(converter.unserializeRDF(response.getBody(), nifformat));
        } catch (Exception e) {
            throw new AssertionFailureException("RDF validation failed");
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

    //Used for constructiong Templates with sparql queries in E-link and E-Link Security Test
    String constructTemplate(String label, String query, String endpoint, String description) {
        query = query.replaceAll("\n","\\\\n");
        return  " {\n" +
                "\"label\":\""+ label + "\",\n"+
                " \"query\":\""+query+"\",\n" +
                " \"endpoint\":\""+endpoint+"\",\n" +
                "\"description\":\""+ description + "\"\n"+
                " }";
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
}
