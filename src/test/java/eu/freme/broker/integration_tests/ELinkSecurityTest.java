package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 01.10.2015.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FremeCommonConfig.class)
@ActiveProfiles("broker")
public class ELinkSecurityTest extends IntegrationTest {


    //@Autowired
    //AbstractAccessDecisionManager decisionManager;

    //@Autowired
    //AccessLevelHelper accessLevelHelper;


    private static final String usernameWithPermission = "userwithpermission";
    private static final String passwordWithPermission = "testpassword";
    private static final String usernameWithoutPermission = "userwithoutpermission";
    private static final String passwordWithoutPermission = "testpassword";
    private static String tokenWithPermission;
    private static String tokenWithOutPermission;
    private static String tokenAdmin;
    private static boolean initialized = false;


    public ELinkSecurityTest() throws UnirestException {
        super("/e-link/");
    }


    public void initUser() throws UnirestException {
        //Creates two users, one intended to have permission, the other not
        createUser(usernameWithPermission, passwordWithPermission);
        tokenWithPermission = authenticateUser(usernameWithPermission, passwordWithPermission);
        createUser(usernameWithoutPermission, passwordWithoutPermission);
        tokenWithOutPermission = authenticateUser(usernameWithoutPermission, passwordWithoutPermission);
        ConfigurableApplicationContext context = IntegrationTestSetup.getApplicationContext();
        tokenAdmin = authenticateUser(context.getEnvironment().getProperty("admin.username"), context.getEnvironment().getProperty("admin.password"));
        initialized = true;
    }

    @Test
    public void invalidTemplateId() throws Exception{
        if (!initialized)
            initUser();

        // add a template for the first user
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "private", tokenWithPermission);
        assertNotNull(templateid);

        assertEquals(HttpStatus.NOT_FOUND.value(), deleteTemplate("999", tokenWithPermission));
        assertEquals(HttpStatus.NOT_FOUND.value(), getTemplate("999", tokenWithPermission));
        assertEquals(HttpStatus.NOT_FOUND.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "999", tokenWithPermission, "public"));
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");
        assertEquals(HttpStatus.NOT_FOUND.value(), doELink(nifContent, "999", tokenWithOutPermission));

        int responseCode = deleteTemplate(templateid, tokenWithPermission);
        assertTrue(responseCode == HttpStatus.OK.value() || responseCode == HttpStatus.NO_CONTENT.value());
    }

    @Test

    public void noAuthentication() throws Exception {
        if (!initialized)
            initUser();
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "public", tokenWithPermission);
        assertNotNull(templateid);

        //try {
            createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "public", null);
        //}catch(AccessDeniedException e){

        //}
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid), null));
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, null));
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent, templateid, null));
        assertEquals(HttpStatus.FORBIDDEN.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, null, "private"));
        assertEquals(HttpStatus.FORBIDDEN.value(), deleteTemplate(templateid, null));

        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid), tokenWithPermission));
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, tokenWithPermission));
        assertEquals(HttpStatus.OK.value(), doELink(nifContent, templateid, tokenWithPermission));
        assertEquals(HttpStatus.OK.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission, "private"));
        assertEquals(204, deleteTemplate(templateid, tokenWithPermission));



    }

    @Test
    public void testTemplateHandlingWithSecuritySimple() throws Exception {
        if (!initialized)
            initUser();

        // add a template for the first user
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "private", tokenWithPermission);
        assertNotNull(templateid);

        String templateid1 = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "public", tokenWithPermission);
        assertNotNull(templateid1);

        String templateid2 = createTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", "private", tokenWithOutPermission);
        assertNotNull(templateid2);

        //should return two templates
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid1, templateid2), tokenWithOutPermission));

        //assertEquals(HttpStatus.OK.value(),getAllTemplates(tokenWithPermission));
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, tokenWithPermission));
    }

    @Test
    public void testTemplateHandlingWithSecurity() throws Exception {

        if (!initialized)
            initUser();

        // add a template for the first user
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "private", tokenWithPermission);
        assertNotNull(templateid);

        String templateid2 = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "private", tokenWithPermission);
        assertNotNull(templateid2);

        assertEquals(getTemplate(templateid2, tokenWithPermission), HttpStatus.OK.value());

        // User without permission should not be able to query, update or delete another user's template
        // User with permission should
        // check query template...
        assertEquals(getTemplate(templateid, tokenWithPermission), HttpStatus.OK.value());
        assertEquals(getTemplate(templateid, tokenWithOutPermission), HttpStatus.FORBIDDEN.value());
        // check update template...
        assertEquals(updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithOutPermission, "private"), HttpStatus.FORBIDDEN.value());
        assertEquals(updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission, "public"), HttpStatus.OK.value());
        assertEquals(getTemplate(templateid, tokenWithOutPermission), HttpStatus.OK.value());
        assertEquals(updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission, "private"), HttpStatus.OK.value());
        assertEquals(getTemplate(templateid, tokenWithOutPermission), HttpStatus.FORBIDDEN.value());

        // only admin can change the metadata. should fail
        /*assertEquals(HttpStatus.UNAUTHORIZED.value(), updateTemplateMetadata(templateid, usernameWithoutPermission, "private", tokenWithPermission));
        // change user
        assertEquals(HttpStatus.OK.value(), updateTemplateMetadata(templateid, usernameWithoutPermission, "private",tokenAdmin));
        assertEquals(getTemplate(templateid, tokenWithOutPermission), HttpStatus.OK.value());
        assertEquals(getTemplate(templateid, tokenWithPermission), HttpStatus.FORBIDDEN.value());

        assertEquals(HttpStatus.OK.value(), updateTemplateMetadata(templateid, usernameWithoutPermission, "public", tokenAdmin));
        assertEquals(getTemplate(templateid, tokenWithPermission), HttpStatus.OK.value());
        assertEquals(deleteTemplate(templateid, tokenWithPermission), HttpStatus.FORBIDDEN.value());


        assertEquals(HttpStatus.OK.value(), updateTemplateMetadata(templateid, usernameWithPermission, "private", tokenAdmin));
*/


        // check delete template...
        assertEquals(deleteTemplate(templateid, tokenWithOutPermission), HttpStatus.FORBIDDEN.value());
        int responseCode = deleteTemplate(templateid, tokenWithPermission);
        assertTrue(responseCode == HttpStatus.OK.value() || responseCode == HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void testELinkDocuments() throws Exception {

        if (!initialized)
            initUser();

        //Adds template temporarily
        String id = createTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", "private", tokenWithPermission);

        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");

        // this shouldn't be granted...
        assertEquals(HttpStatus.FORBIDDEN.value(), doELink(nifContent, id, tokenWithOutPermission));
        // but this...
        assertEquals(HttpStatus.OK.value(), doELink(nifContent, id, tokenWithPermission));

        //Deletes temporary template
        deleteTemplate(id, tokenWithPermission);
    }

    private int doELink(String nifContent, String templateId, String token) throws UnirestException, IOException {
        HttpResponse<String> response = baseRequestPost("documents", token)
                .queryString("templateid", templateId)
                .queryString("informat", "turtle")
                .queryString("outformat", "turtle")
                .body(nifContent)
                .asString();
        if(response.getStatus()==HttpStatus.OK.value()) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
        }
        return response.getStatus();
    }

    //Tests GET e-link/templates/
    public int getAllTemplates(List<String> expectedIDs, String token) throws UnirestException, IOException {
        HttpResponse<String> response;

        response = baseRequestGet("templates", token)
                .queryString("outformat", "json").asString();


        if (response.getStatus() == HttpStatus.OK.value()) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.JSON);
        }
        JSONArray jsonArray = new JSONArray(response.getBody());
        assertEquals(expectedIDs.size(), jsonArray.length());

        ArrayList<String> tempIDs = new ArrayList<>(expectedIDs);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            assertTrue(tempIDs.remove(id));
        }
        return response.getStatus();
    }

    //Tests POST e-link/templates/
    public String createTemplate(String filename, String visibility, String token) throws Exception {
        String query = readFile(filename);

        HttpResponse<String> response = baseRequestPost("templates", token)
                .queryString("visibility", visibility)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate("Some label", query, "http://dbpedia.org/sparql/", "Some description"))
                .asString();

        if(response.getStatus() == HttpStatus.FORBIDDEN.value())
            throw new AccessDeniedException("Creation of template denied.");
        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);

        JSONObject jsonObj = new JSONObject(response.getBody());

        String id = jsonObj.getString("templateId");
        // check, if id is numerical
        assertTrue(id.matches("\\d+"));

        return id;
    }

    //Tests GET e-link/templates/
    public int getTemplate(String id, String token) throws UnirestException, IOException {

        HttpResponse<String> response = baseRequestGet("templates/" + id, token)
                .queryString("outformat", "json").asString();
        if (response.getStatus() == HttpStatus.OK.value()) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.JSON);
        }
        return response.getStatus();
    }

    //Tests PUT e-link/templates/
    public int updateTemplate(String filename, String id, String token, String visibility) throws IOException, UnirestException {
        String query = readFile(filename);

        HttpResponse<String> response = baseRequestPut("templates/" + id, token)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .queryString("visibility", visibility)
                .body(constructTemplate("Some label", query, "http://dbpedia.org/sparql/", "Some description"))
                .asString();

        if (response.getStatus() == HttpStatus.OK.value()) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);
            JSONObject jsonObj = new JSONObject(response.getBody());
            String newid = jsonObj.getString("templateId");
            // check, if id is numerical
            assertTrue(newid.matches("\\d+"));
            assertTrue(id.equals(newid));
        }

        return response.getStatus();

    }

    public int updateTemplateMetadata(String id, String ownerName, String visibility, String token) throws UnirestException {
        HttpResponse<String> response = baseRequestPut("templates/admin/" + id, token)
                .queryString("owner", ownerName)
                .queryString("visibility", visibility)
                .asString();
        return response.getStatus();
    }

    //Tests DELETE e-link/templates/
    public int deleteTemplate(String id, String token) throws UnirestException {
        HttpResponse<String> response = baseRequestDelete("templates/" + id, token)
                .asString();
        return response.getStatus();
    }
}
