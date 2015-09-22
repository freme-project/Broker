/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.conversion.rdf.RDFConstants;

import eu.freme.conversion.rdf.RDFConversionService;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import static org.junit.Assert.*;


/**
 * Created by Jonathan Sauder - jsauder@campus.tu-berlin.de on 28.07.15.
 */
public class ELinkTestSecurity extends IntegrationTest {


    //@Autowired
    //AbstractAccessDecisionManager decisionManager;

    //@Autowired
    //AccessLevelHelper accessLevelHelper;


    private static final String usernameWithPermission = "userwithpermission";
    private static final String passwordWithPermission  = "testpassword";
    private static final String usernameWithoutPermission = "userwithoutpermission";
    private static final String passwordWithoutPermission  = "testpassword";
    private static String tokenWithPermission;
    private static String tokenWithOutPermission;
    private static String tokenAdmin;
    private static boolean initialized = false;


    public ELinkTestSecurity() throws UnirestException{
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
    public void testTemplateHandlingWithSecuritySimple() throws Exception {
        if(!initialized)
            initUser();

        // add a template for the first user
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", tokenWithPermission);
        assertNotNull(templateid);
    }

    @Test
    public void testTemplateHandlingWithSecurity() throws Exception{

        if(!initialized)
            initUser();

        // add a template for the first user
        String templateid = createTemplate("src/test/resources/rdftest/e-link/sparql1.ttl", tokenWithPermission);
        assertNotNull(templateid);

        // User without permission should not be able to query, update or delete another user's template
        // User with permission should
        // check query template...
        assertEquals(getTemplate(templateid, tokenWithPermission), HttpStatus.OK.value());
        assertEquals(getTemplate(templateid, tokenWithOutPermission), HttpStatus.FORBIDDEN.value());
        // check update template...
        assertEquals(updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithOutPermission, "private"),  HttpStatus.FORBIDDEN.value());
        assertEquals(updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission, "public"),  HttpStatus.OK.value());
        assertEquals(getTemplate(templateid, tokenWithOutPermission), HttpStatus.OK.value());
        assertEquals(updateTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission, "private"),  HttpStatus.OK.value());
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
        assertTrue(responseCode== HttpStatus.OK.value() || responseCode == HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void testELinkDocuments() throws Exception {

        if(!initialized)
            initUser();

        //Adds template temporarily
        String id = createTemplate("src/test/resources/rdftest/e-link/sparql3.ttl", tokenWithPermission);

        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");

        // this shouldn't be granted...
        HttpResponse<String> response = baseRequestPost("documents")
                .header("X-Auth-Token", tokenWithOutPermission)
                .queryString("templateid", id)
                .queryString("informat", "turtle")
                .queryString("outformat", "turtle")
                .body(nifContent)
                .asString();
        assertEquals(response.getStatus(), HttpStatus.FORBIDDEN.value());
        // but this...
        response = baseRequestPost("documents")
                .header("X-Auth-Token", tokenWithPermission)
                .queryString("templateid", id)
                .queryString("informat", "turtle")
                .queryString("outformat", "turtle")
                .body(nifContent)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
        //Deletes temporary template
        deleteTemplate(id, tokenWithPermission);
    }

    //Tests GET e-link/templates/
    public void testELinkTemplates(String token) throws UnirestException, IOException {
        HttpResponse<String> response;

        response = baseRequestGet("templates")
                .header("X-Auth-Token", token)
                .queryString("outformat", "json-ld").asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);

    }

    //Tests POST e-link/templates/
    public String createTemplate(String filename, String token) throws Exception {
        String query = readFile(filename);

        HttpResponse<String> response = baseRequestPost("templates")
                .header("X-Auth-Token", token)
                .queryString("visibility", "private")
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
    public int getTemplate(String id, String token) throws UnirestException, IOException {
        HttpResponse<String> response = baseRequestGet("templates/"+id)
                .header("X-Auth-Token", token)
                .queryString("outformat", "turtle")
                .asString();
        if(response.getStatus()==HttpStatus.OK.value()) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
        }
        return response.getStatus();
    }

    //Tests PUT e-link/templates/
    public int updateTemplate(String filename, String id, String token, String visibility) throws IOException, UnirestException{
        String query = readFile(filename);

        HttpResponse<String> response = baseRequestPut("templates/" + id)
                .header("X-Auth-Token",token)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .queryString("visibility", visibility)
                .body(constructTemplate("Some label", query, "http://dbpedia.org/sparql/", "Some description"))
                .asString();

        if(response.getStatus() == HttpStatus.OK.value()) {
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
        HttpResponse<String> response = baseRequestPut("templates/admin/" + id)
                .header("X-Auth-Token",token)
                .queryString("owner", ownerName)
                .queryString("visibility", visibility)
                .asString();
        return response.getStatus();
    }

    //Tests DELETE e-link/templates/
    public int deleteTemplate(String id, String token) throws UnirestException{
        HttpResponse<String> response = baseRequestDelete("templates/" + id)
                .header("X-Auth-Token", token)
                .asString();
        return response.getStatus();
    }




}
