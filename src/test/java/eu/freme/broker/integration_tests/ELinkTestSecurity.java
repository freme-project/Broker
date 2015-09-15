package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.broker.eservices.BaseRestController;
import eu.freme.broker.security.database.model.Template;
import eu.freme.broker.security.database.model.User;
import eu.freme.broker.security.database.repository.TemplateRepository;
import eu.freme.broker.security.database.repository.UserRepository;
import eu.freme.broker.security.tools.AccessLevelHelper;
import eu.freme.conversion.rdf.RDFConstants;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


/**
 * Created by Jonathan Sauder - jsauder@campus.tu-berlin.de on 28.07.15.
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



    String usernameWithPermission = "userwithpermission";
    String passwordWithPermission  = "testpassword";
    String usernameWithoutPermission = "userwithoutpermission";
    String passwordWithoutPermission  = "testpassword";


    public ELinkTestSecurity() throws UnirestException{
        super("/e-link/");
    }

    @Test
    public void testTemplateHandlingWithSecurity() throws Exception{


        //Creates two users, one intended to have permission, the other not
        createUser(usernameWithPermission, passwordWithPermission);
        String tokenWithPermission = authenticateUser(usernameWithPermission, passwordWithPermission);
        createUser(usernameWithoutPermission, passwordWithoutPermission);
        String tokenWithOutPermission = authenticateUser(usernameWithoutPermission, passwordWithoutPermission);

        //Tests if users can add  and update Templates
        String templateid = testELinkTemplatesAdd("src/test/resources/rdftest/e-link/sparql1.ttl", tokenWithPermission);
        assertNotNull(templateid);

        //Tests if template is successfully created and has right attributes
        /*
        Template fromDB = templateRepository.findOneById(templateid);
        assertNotNull(fromDB);
        assertTrue(fromDB.getOwner().getName().equals(usernameWithPermission));
        */
        testELinkTemplatesUpdate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission);


        //User without permission should not be able to delete another user's template
        assertFalse(testELinkTemplatesDelete(templateid,tokenWithOutPermission));

        //User with permission should
        assertTrue(testELinkTemplatesDelete(templateid, tokenWithPermission));

    }

    //Tests GET e-link/templates/
    public void testELinkTemplates() throws UnirestException, IOException {
        HttpResponse<String> response;

        response = baseRequestGet("templates")
                .queryString("outformat", "json-ld").asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);

    }

    //Tests POST e-link/templates/
    public String testELinkTemplatesAdd(String filename, String token) throws Exception {
        String query = readFile(filename);



        HttpResponse<String> response = baseRequestPost("templates")
                .header("X-Auth-Token", token)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate("Some label", query, "http://dbpedia.org/sparql/", "Some description"))
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
    public void testELinkTemplatesUpdate(String filename, String id, String token) throws IOException, UnirestException{
        String query = readFile(filename);

        HttpResponse<String> response = baseRequestPut("templates/" + id)
                .header("X-Auth-Token",token)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate("Some label", query, "http://dbpedia.org/sparql/", "Some description"))
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);

        JSONObject jsonObj = new JSONObject(response.getBody());
        String newid = jsonObj.getString("templateId");
        // check, if id is numerical
        assertTrue(newid.matches("\\d+"));
        assertTrue(id.equals(newid));



    }

    //Tests DELETE e-link/templates/
    public boolean testELinkTemplatesDelete(String id, String token) throws UnirestException{
        HttpResponse<String> response = baseRequestDelete("templates/" + id)
                .header("X-Auth-Token", token)
                .asString();
        return (response.getStatus() == 200 || response.getStatus() == 204);
    }




}
