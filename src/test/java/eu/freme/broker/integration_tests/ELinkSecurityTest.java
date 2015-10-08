package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.json.JSONArray;
import org.json.JSONObject;
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

import static com.github.stefanbirkner.fishbowl.Fishbowl.exceptionThrownBy;
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

    public ELinkSecurityTest() throws UnirestException {
        super("/e-link/");
        enableAuthenticate();
    }


    @Test
    public void invalidTemplateId() throws Exception{


        // add a template for the first user
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "private", tokenWithPermission);
        assertNotNull(templateid);

        assertEquals(HttpStatus.NOT_FOUND.value(), deleteTemplate("999", tokenWithPermission));
        assertEquals(HttpStatus.NOT_FOUND.value(), getTemplate("999", tokenWithPermission));
        assertEquals(HttpStatus.NOT_FOUND.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "999", tokenWithPermission, "public"));
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");
        assertEquals(HttpStatus.NOT_FOUND.value(), doELink(nifContent, "999", tokenWithOutPermission));


        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid, tokenWithPermission));
    }

    @Test

    public void testAnonymousUser() throws Exception {
        logger.info("testAnonymousUser");


        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "public", tokenWithPermission);
        assertNotNull(templateid);

        logger.info("try to create template as anonymous user... should not work");
        Throwable exception = exceptionThrownBy(() -> createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "public", null));
        assertEquals(AccessDeniedException.class, exception.getClass());
        logger.info("try to fetch all templates as anonymous user... should work");
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid), null));
        logger.info("try to get public template as anonymous user... should work");
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, null));
        logger.info("try to use e-link with public template as anonymous user... should work");
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent, templateid, null));
        logger.info("try to update a public template as anonymous user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, null, "private"));
        logger.info("try to delete public template as anonymous user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), deleteTemplate(templateid, null));

        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid, tokenWithPermission));
    }

    @Test
    public void testGetAllTemplates() throws Exception {
        logger.info("testGetAllTemplates");


        // add a template for the first user
        logger.info("create private template for user 1");
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "private", tokenWithPermission);
        logger.info("created template with id: " + templateid);
        assertNotNull(templateid);
        logger.info("create public template for user 1");
        String templateid1 = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "public", tokenWithPermission);
        logger.info("created template with id: " + templateid1);
        assertNotNull(templateid1);
        logger.info("create private template for user 2");
        String templateid2 = createTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", "private", tokenWithOutPermission);
        logger.info("created template with id: " + templateid2);
        assertNotNull(templateid2);

        logger.info("getAllTemplates called by user 2 should return template " + templateid1 + " and " + templateid2);
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid1, templateid2), tokenWithOutPermission));

        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid, tokenWithPermission));
        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid1, tokenWithPermission));
        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid2, tokenWithOutPermission));
    }

    @Test
    public void testTemplateHandlingWithSecurity() throws Exception {
        logger.info("testTemplateHandlingWithSecurity");


        // add a template for the first user
        logger.info("create private template for user 1");
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", "private", tokenWithPermission);
        logger.info("private template for user 1 has id: " + templateid);
        assertNotNull(templateid);

        // User without permission should not be able to query, update or delete another user's private template
        // User with permission should
        // Public templates can be queried, but not updated or deleted by another user.
        logger.info("try to fetch private template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), getTemplate(templateid, tokenWithOutPermission));
        logger.info("try to fetch private template as owner... should work");
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, tokenWithPermission));
        logger.info("fetch all templates as other user... should return an empty list");
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(), tokenWithOutPermission));
        logger.info("fetch all templates as owner... should return template: "+templateid);
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid), tokenWithPermission));
        logger.info("try to update private template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithOutPermission, "private"));
        logger.info("try to delete private template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), deleteTemplate(templateid, tokenWithOutPermission));

        logger.info("try to update private template as owner. Set visibility to public... should work");
        assertEquals(HttpStatus.OK.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission, "public"));

        logger.info("try to fetch public template as other user... should work");
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, tokenWithOutPermission));
        logger.info("try to fetch public template as owner... should work");
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, tokenWithPermission));
        logger.info("fetch all templates as other user... should return template: " + templateid);
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid), tokenWithOutPermission));
        logger.info("fetch all templates as owner... should return template: "+templateid);
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid), tokenWithPermission));
        logger.info("try to update public template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithOutPermission, "private"));
        logger.info("try to delete public template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), deleteTemplate(templateid, tokenWithOutPermission));

        logger.info("try to set public template to private as owner. Set visibility to private... should work");
        assertEquals(HttpStatus.OK.value(), updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission, "private"));
        logger.info("re-try to fetch private template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), getTemplate(templateid, tokenWithOutPermission));

        logger.info("try to delete private template as owner... should work");
        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid, tokenWithPermission));
    }

    @Test
    public void testELinkDocuments() throws Exception {
        logger.info("testELinkDocuments");


        logger.info("create private template");
        String id = createTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", "private", tokenWithPermission);

        logger.info("create public template");
        String idPublic = createTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", "public", tokenWithPermission);

        logger.info("read nif to enrich");
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");

        logger.info("try to enrich via private template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), doELink(nifContent, id, tokenWithOutPermission));
        logger.info("try to enrich via private template as template owner... should work");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent, id, tokenWithPermission));
        logger.info("try to enrich via public template as other user... should work");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent, idPublic, tokenWithOutPermission));
        logger.info("try to enrich via public template as template owner... should work");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent, idPublic, tokenWithPermission));

        logger.info("delete private template");
        deleteTemplate(id, tokenWithPermission);
        logger.info("delete public template");
        deleteTemplate(idPublic, tokenWithPermission);
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

        if(response.getStatus() == HttpStatus.UNAUTHORIZED.value())
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
