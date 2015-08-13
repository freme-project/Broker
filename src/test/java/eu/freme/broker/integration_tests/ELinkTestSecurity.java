package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.eservices.BaseRestController;
import eu.freme.broker.security.database.TemplateRepository;
import eu.freme.broker.security.database.User;
import eu.freme.broker.security.database.UserRepository;
import eu.freme.broker.security.tools.AccessLevelHelper;
import eu.freme.conversion.rdf.RDFConstants;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


/**
 * Created by jonathan on 28.07.15.
 */
public class ELinkTestSecurity extends IntegrationTest {


    @Autowired
    AbstractAccessDecisionManager decisionManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccessLevelHelper accessLevelHelper;

    @Autowired
    TemplateRepository templateRepository;

    String token;

    public ELinkTestSecurity() throws UnirestException{
        super("/e-link/");


    }


    public void createAndAuthenticateUser() throws UnirestException{

        String username = "testuser";
        String password  = "testpassword";
        //System.out.println("ASDASD "+getBaseURL());
        HttpResponse<String> response = Unirest.post(getBaseURL() + "/user")
                .queryString("username", username)
                .queryString("password", password).asString();
        logger.debug("STATUS: "+response.getStatus());
        assertTrue(response.getStatus() == HttpStatus.OK.value());


        /*
        logger.info("login with new user / create token");
        response = Unirest
                .post(getBaseURL()  + BaseRestController.authenticationEndpoint)
                .header("X-Auth-Username", username)
                .header("X-Auth-Password", password).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
        token = new JSONObject(response.getBody()).getString("token");
        //String responseUsername = new JSONObject(response.getBody()).getString("name");
        //assertTrue(username.equals(responseUsername));
*/
    }

    @Test
    public void testSmal() throws Exception{
        createAndAuthenticateUser();
        String templateid = testELinkTemplatesAdd("src/test/resources/rdftest/e-link/sparql1.ttl");
    }

    //Tests Creation, fetching, modification and deletion of a template and fetching of all templates
    @Test
    public void testTemplateHandling() throws Exception{
        String templateid = testELinkTemplatesAdd("src/test/resources/rdftest/e-link/sparql1.ttl");
        testELinkTemplatesId(templateid);
        testELinkTemplatesUpdate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid);
        testELinkTemplates();
        testELinkTemplatesDelete(templateid);
    }

    //Tests GET e-link/templates/
    public void testELinkTemplates() throws UnirestException, IOException {
        HttpResponse<String> response;

        response = baseRequestGet("templates")
                .queryString("outformat", "json-ld").asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);

    }

    //Tests POST /e-link/documents/
    @Test
    @Ignore //TODO: wait for issue: POST /e-link/documents response has wrong Content-Type header #26 https://github.com/freme-project/e-Link/issues/26
    public void testELinkDocuments() throws Exception {
        //Adds template temporarily
        String id = testELinkTemplatesAdd("src/test/resources/rdftest/e-link/sparql3.ttl");

        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");

        HttpResponse<String> response = baseRequestPost("documents")
                .queryString("templateid", id)
                .queryString("informat", "turtle")
                .queryString("outformat", "turtle")
                .body(nifContent)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
        //Deletes temporary template
        testELinkTemplatesDelete(id);
    }

    //// HELPER METHODS

    //Tests POST e-link/templates/
    public String testELinkTemplatesAdd(String filename) throws Exception {
        String query = readFile(filename);



        HttpResponse<String> response = baseRequestPost("templates")
                .header("X-Auth-Token", token)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate(query, "http://dbpedia.org/sparql/"))
        .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);

        JSONObject jsonObj = new JSONObject(response.getBody());

        String id = jsonObj.getString("templateId");
        // check, if id is numerical
        assertTrue(id.matches("\\d+"));

        return id;
    }

    //Tests GET e-link/templates/
    public void testELinkTemplatesId(String id) throws UnirestException, IOException {
        HttpResponse<String> response = baseRequestGet("templates/"+id)
                .queryString("outformat", "turtle")
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

    //Tests PUT e-link/templates/
    public void testELinkTemplatesUpdate(String filename, String id) throws IOException, UnirestException{
        String query = readFile(filename);

        HttpResponse<String> response = baseRequestPut("templates/"+id)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate(query, "http://dbpedia.org/sparql/"))
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);

        JSONObject jsonObj = new JSONObject(response.getBody());
        String newid = jsonObj.getString("templateId");
        // check, if id is numerical
        assertTrue(newid.matches("\\d+"));
        assertTrue(id.equals(newid));



    }

    //Tests DELETE e-link/templates/
    public void testELinkTemplatesDelete(String id) throws UnirestException{
        HttpResponse<String> response = baseRequestDelete("templates/" + id).asString();
        assertTrue(response.getStatus() == 200 || response.getStatus() == 204);
    }

}
