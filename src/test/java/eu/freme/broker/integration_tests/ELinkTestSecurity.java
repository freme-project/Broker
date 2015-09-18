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

import static org.junit.Assert.*;


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

        assertEquals(testELinkTemplatesId(templateid, tokenWithPermission), HttpStatus.OK.value());
        assertEquals(testELinkTemplatesId(templateid, tokenWithOutPermission), HttpStatus.FORBIDDEN.value());

        assertEquals(testELinkTemplatesUpdate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithOutPermission),  HttpStatus.FORBIDDEN.value());
        assertEquals(testELinkTemplatesUpdate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid, tokenWithPermission),  HttpStatus.OK.value());


        //User without permission should not be able to delete another user's template
        assertEquals(testELinkTemplatesDelete(templateid, tokenWithOutPermission), HttpStatus.FORBIDDEN.value());

        //User with permission should
        int responseCode = testELinkTemplatesDelete(templateid, tokenWithPermission);
        assertTrue(responseCode== HttpStatus.OK.value() || responseCode == HttpStatus.NO_CONTENT.value());

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
    public int testELinkTemplatesId(String id, String token) throws UnirestException, IOException {
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
    public int testELinkTemplatesUpdate(String filename, String id, String token) throws IOException, UnirestException{
        String query = readFile(filename);

        HttpResponse<String> response = baseRequestPut("templates/" + id)
                .header("X-Auth-Token",token)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate("Some label", query, "http://dbpedia.org/sparql/", "Some description"))
                .asString();

        if(response.getStatus() == 200) {
            validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);
            JSONObject jsonObj = new JSONObject(response.getBody());
            String newid = jsonObj.getString("templateId");
            // check, if id is numerical
            assertTrue(newid.matches("\\d+"));
            assertTrue(id.equals(newid));
        }

        return response.getStatus();

    }

    //Tests DELETE e-link/templates/
    public int testELinkTemplatesDelete(String id, String token) throws UnirestException{
        HttpResponse<String> response = baseRequestDelete("templates/" + id)
                .header("X-Auth-Token", token)
                .asString();
        return response.getStatus();
    }




}
