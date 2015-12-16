package eu.freme.broker.integration_tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.Template;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.stefanbirkner.fishbowl.Fishbowl.exceptionThrownBy;
import static org.junit.Assert.*;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 01.10.2015.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FremeCommonConfig.class)
@ActiveProfiles("broker")
public class ELinkSecurityTest extends EServiceTest {

    public ELinkSecurityTest() throws UnirestException {
        super("/e-link/");
        enableAuthenticate();
    }

    private String baseUrl;
    private String mockupEnrichUrl;

    private static final String notFoundException = "eu.freme.common.exception.OwnedResourceNotFoundException || EXCEPTION ~=eu.freme.common.exception.TemplateNotFoundException";

    @Before
    public void replaceBaseUrl(){
        baseUrl= getBaseUrl().replace("localhost","127.0.0.1");
        mockupEnrichUrl = baseUrl+ "/mockups/file/ELINK.ttl";
    }

    @Test
    public void invalidTemplateId() throws Exception{


        // add a template for the first user
        long templateid = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "public"), getTokenWithPermission());
        assertNotNull(templateid);

        loggerIgnore(notFoundException);
        assertEquals(HttpStatus.NOT_FOUND.value(), deleteTemplate(999, getTokenWithPermission()));
        assertEquals(HttpStatus.NOT_FOUND.value(), getTemplate(999, getTokenWithPermission()));
        assertEquals(HttpStatus.NOT_FOUND.value(), updateTemplate(999, getTokenWithPermission(), constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "public"), null));
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");
        assertEquals(HttpStatus.NOT_FOUND.value(), doELink(nifContent, 999, getTokenWithOutPermission()));
        loggerUnignore(notFoundException);

        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid, getTokenWithPermission()));
    }

    @Test
    public void testAnonymousUser() throws Exception {
        logger.info("testAnonymousUser");


        long templateid = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "public"), getTokenWithPermission());
        assertNotNull(templateid);

        logger.info("try to create template as anonymous user... should not work");
        Throwable exception = exceptionThrownBy(() -> createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "public"), null));
        assertEquals(AccessDeniedException.class, exception.getClass());
        logger.info("try to fetch all templates as anonymous user... should work");
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Collections.singletonList(templateid), null));
        logger.info("try to get public template as anonymous user... should work");
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, null));
        logger.info("try to use e-link with public template as anonymous user... should work");
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent, templateid, null));



        loggerIgnore(accessDeniedExceptions);
        logger.info("try to update a public template as anonymous user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), updateTemplate(templateid, null, constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql3.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"),null));
        logger.info("try to delete public template as anonymous user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), deleteTemplate(templateid, null));
        loggerUnignore(accessDeniedExceptions);

        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid, getTokenWithPermission()));
    }

    @Test
    public void testGetAllTemplates() throws Exception {
        logger.info("testGetAllTemplates");


        // add a template for the first user
        logger.info("create private template for user 1");
        long templateid = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"), getTokenWithPermission());
        logger.info("created template with id: " + templateid);
        assertNotNull(templateid);
        logger.info("create public template for user 1");
        long templateid1 = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "public"), getTokenWithPermission());
        logger.info("created template with id: " + templateid1);
        assertNotNull(templateid1);
        logger.info("create private template for user 2");
        long templateid2 = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"), getTokenWithOutPermission());
        logger.info("created template with id: " + templateid2);
        assertNotNull(templateid2);


        logger.info("getAllTemplates called by user 2 should return template " + templateid1 + " and " + templateid2);
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Arrays.asList(templateid1, templateid2), getTokenWithOutPermission()));

        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid, getTokenWithPermission()));
        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid1, getTokenWithPermission()));
        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid2, getTokenWithOutPermission()));
    }

    @Test
    public void testUpdateTemplate() throws Exception {
        logger.info("testUpdateTemplate");
        // add a template for the first user
        logger.info("create private template for user 1");
        long templateid = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"), getTokenWithPermission());
        String template = constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql3.ttl"), mockupEnrichUrl, "Some description", "sparql","public");

        assertEquals(HttpStatus.OK.value(), updateTemplate(templateid, getTokenWithPermission(),
                        template,
                        null)
        );
        //assertEquals(HttpStatus.OK.value(), updateTemplate(templateid, getTokenWithPermission(), template, null));
        //assertEquals(HttpStatus.OK.value(), updateTemplate(templateid, getTokenWithPermission(), template, null));
        assertEquals(HttpStatus.OK.value(), updateTemplate(templateid, getTokenWithPermission(), template, usernameWithoutPermission));

        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid,getTokenWithOutPermission()));
    }


    @Test
    public void testTemplateHandlingWithSecurity() throws Exception {
        logger.info("testTemplateHandlingWithSecurity");


        // add a template for the first user
        logger.info("create private template for user 1");
        long templateid = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"), getTokenWithPermission());
        logger.info("private template for user 1 has id: " + templateid);
        assertNotNull(templateid);

        // User without permission should not be able to query, update or delete another user's private template
        // User with permission should
        // Public templates can be queried, but not updated or deleted by another user.

        loggerIgnore(accessDeniedExceptions);
        logger.info("try to fetch private template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), getTemplate(templateid, getTokenWithOutPermission()));
        loggerUnignore(accessDeniedExceptions);
        logger.info("try to fetch private template as owner... should work");
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, getTokenWithPermission()));
        logger.info("fetch all templates as other user... should return an empty list");
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Collections.emptyList(), getTokenWithOutPermission()));
        logger.info("fetch all templates as owner... should return template: "+templateid);
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Collections.singletonList(templateid), getTokenWithPermission()));
        logger.info("try to update private template as other user... should not work");
        loggerIgnore(accessDeniedExceptions);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), updateTemplate(templateid, getTokenWithOutPermission(),
                constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql3.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"), null));
        logger.info("try to delete private template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), deleteTemplate(templateid, getTokenWithOutPermission()));
        loggerUnignore(accessDeniedExceptions);
        logger.info("try to update private template as owner. Set visibility to public... should work");
        assertEquals(HttpStatus.OK.value(), updateTemplate(templateid, getTokenWithPermission(),
                constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql3.ttl"), mockupEnrichUrl, "Some description", "sparql", "public"), null));

        logger.info("try to fetch public template as other user... should work");
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, getTokenWithOutPermission()));
        logger.info("try to fetch public template as owner... should work");
        assertEquals(HttpStatus.OK.value(), getTemplate(templateid, getTokenWithPermission()));
        logger.info("fetch all templates as other user... should return template: " + templateid);
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Collections.singletonList(templateid), getTokenWithOutPermission()));
        logger.info("fetch all templates as owner... should return template: "+templateid);
        assertEquals(HttpStatus.OK.value(), getAllTemplates(Collections.singletonList(templateid), getTokenWithPermission()));
        logger.info("try to update public template as other user... should not work");
        loggerIgnore(accessDeniedExceptions);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), updateTemplate(templateid, getTokenWithOutPermission(),
                constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql3.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"),  null));
        logger.info("try to delete public template as other user... should not work");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), deleteTemplate(templateid, getTokenWithOutPermission()));
        loggerUnignore(accessDeniedExceptions);
        logger.info("try to set public template to private as owner. Set visibility to private... should work");
        assertEquals(HttpStatus.OK.value(), updateTemplate(templateid, getTokenWithPermission(),
                constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql3.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"), null));
        logger.info("re-try to fetch private template as other user... should not work");
        loggerIgnore(accessDeniedExceptions);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), getTemplate(templateid, getTokenWithOutPermission()));
        loggerUnignore(accessDeniedExceptions);
        logger.info("try to delete private template as owner... should work");
        assertEquals(HttpStatus.OK.value(), deleteTemplate(templateid, getTokenWithPermission()));
    }


    @Test
    public void testELinkDocuments() throws Exception {

        logger.info("testELinkDocuments");

        logger.info("create private template");
        long id = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "private"), getTokenWithPermission());

        logger.info("create public template");
        long idPublic = createTemplate(constructTemplate("Some label", readFile("src/test/resources/rdftest/e-link/sparql1.ttl"), mockupEnrichUrl, "Some description", "sparql", "public"), getTokenWithPermission());

        logger.info("read nif to enrich");
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");
        try {
            logger.info("try to enrich via private template as other user... should not work");
            loggerIgnore(accessDeniedExceptions);
            assertEquals(HttpStatus.UNAUTHORIZED.value(), doELink(nifContent, id, getTokenWithOutPermission()));
            loggerUnignore(accessDeniedExceptions);
            logger.info("try to enrich via private template as template owner... should work");
            assertEquals(HttpStatus.OK.value(), doELink(nifContent, id, getTokenWithPermission()));
            logger.info("try to enrich via public template as other user... should work");
            assertEquals(HttpStatus.OK.value(), doELink(nifContent, idPublic, getTokenWithOutPermission()));
            logger.info("try to enrich via public template as template owner... should work");
            assertEquals(HttpStatus.OK.value(), doELink(nifContent, idPublic, getTokenWithPermission()));
        } finally {
            logger.info("delete private template");
            deleteTemplate(id, getTokenWithPermission());
            logger.info("delete public template");
            deleteTemplate(idPublic, getTokenWithPermission());
        }
    }

    @Test
    public void TestELinkExploreSparqlMockup() throws UnirestException, IOException {
        HttpResponse<String> response;

        String rdf_resource = "http://dbpedia.org/resource/Berlin";
        String endpoint = baseUrl+"/mockups/file/EXPLORE-Berlin.ttl";

        rdf_resource = "http://dbpedia.org/resource/Berlin";

        response= post("/explore")
                .queryString("informat","turtle")
                .queryString("outformat","turtle")
                .queryString("endpoint-type","sparql")
                .queryString("resource", rdf_resource)
                .queryString("endpoint", endpoint)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

    @Test
    public void TestELinkExploreLdfMockup() throws UnirestException, IOException {

        String rdf_resource = "http://dbpedia.org/resource/Berlin";
        String endpoint = baseUrl+"/mockups/file/EXPLORE-ldf-resource-Berlin.ttl";

        HttpResponse<String> response = post("/explore")
                .queryString("informat","turtle")
                .queryString("outformat","turtle")
                .queryString("endpoint-type","ldf")
                .queryString("resource", rdf_resource)
                .queryString("endpoint", endpoint)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

    }

    private int doELink(String nifContent, long templateId, String token) throws UnirestException, IOException {
        HttpResponse<String> response = addAuthentication(post("documents"), token)
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
    public int getAllTemplates(List<Long> expectedIDs, String token) throws UnirestException, IOException {
        HttpResponse<String> response;

        response = addAuthentication(get("templates"), token)
                .queryString("outformat", "json").asString();


        if (response.getStatus() == HttpStatus.OK.value()) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.JSON);
        }
        JSONArray jsonArray = new JSONArray(response.getBody());
        assertEquals(expectedIDs.size(), jsonArray.length());

        ArrayList<Long> tempIDs = new ArrayList<>(expectedIDs);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            long id = jsonObject.getLong("id");
            assertTrue(tempIDs.remove(id));
        }
        return response.getStatus();
    }

    //Tests POST e-link/templates/
    public long createTemplate(String template, String token) throws Exception {
        //String query = readFile(filename);

        HttpResponse<String> response = addAuthentication(post("templates"), token)
                .queryString("informat", "json")
                .queryString("outformat", "json")
                .body(template)//constructTemplate("Some label", template, mockupEnrichUrl, "Some description", "sparql", visibility))
                .asString();

        if(response.getStatus() == HttpStatus.UNAUTHORIZED.value())
            throw new AccessDeniedException("Creation of template denied.");
        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON);

        JSONObject jsonObj = new JSONObject(response.getBody());

        long id = jsonObj.getLong("id");
        // check, if id is numerical
        //assertTrue(id.matches("\\d+"));

        return id;
    }

    //Tests GET e-link/templates/
    public int getTemplate(long id, String token) throws UnirestException, IOException {

        HttpResponse<String> response = addAuthentication(get("templates/" + id), token)
                .queryString("outformat", "json").asString();
        if (response.getStatus() == HttpStatus.OK.value()) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.JSON);
        }
        return response.getStatus();
    }


    //Tests PUT e-link/templates/
    public int updateTemplate(long id, String token, String template, String owner) throws IOException, UnirestException {
        //String query = readFile(filename);

        HttpRequestWithBody request = addAuthentication(put("templates/" + id), token)
                .queryString("informat", "json")
                .queryString("outformat", "json")
                        //.queryString("visibility", visibility)
                        //.queryString("type", type)
                .queryString("owner", owner);

        HttpResponse<String> response;
        if(template!=null)
            response = request.body(template).asString();
        else
            response = request.asString();

        if (response.getStatus() == HttpStatus.OK.value()) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.JSON);
            JSONObject jsonObj = new JSONObject(response.getBody());
            long newid = jsonObj.getLong("id");
            // check, if id is numerical

            assertEquals(id, newid);
            /*if(!Strings.isNullOrEmpty(visibility)) {
                String newVisibility = jsonObj.getString("visibility");
                assertEquals(visibility.toLowerCase(), newVisibility.toLowerCase());
            }
            if(!Strings.isNullOrEmpty(type)){
                String newType = jsonObj.getString("type");
                assertEquals(type.toLowerCase(), newType.toLowerCase());
            }*/
            if(!Strings.isNullOrEmpty(owner)) {
                String newOwner = jsonObj.getJSONObject("owner").getString("name");
                assertEquals(owner, newOwner);
            }
        }

        return response.getStatus();

    }

    //Tests DELETE e-link/templates/
    public int deleteTemplate(long id, String token) throws UnirestException {
        HttpResponse<String> response = addAuthentication(delete("templates/" + id), token)
                .asString();
        return response.getStatus();
    }

    //Used for constructiong Templates with sparql queries in E-link and E-Link Security Test
    public static String constructTemplate(String label, String query, String endpoint, String description, String endpointType, String visibility) throws JsonProcessingException {
        Template template = new Template(null, OwnedResource.Visibility.getByString(visibility), Template.Type.getByString(endpointType), endpoint, query, label, description);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String serialization = ow.writeValueAsString(template);
        return serialization;
    }
}
