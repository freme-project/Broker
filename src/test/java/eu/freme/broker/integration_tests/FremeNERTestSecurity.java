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
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.broker.security.database.dao.DatasetDAO;
import eu.freme.broker.security.database.dao.UserDAO;
import eu.freme.broker.security.database.model.Dataset;
import eu.freme.broker.security.database.model.OwnedResource;
import eu.freme.broker.security.database.model.User;
import eu.freme.broker.security.database.repository.DatasetRepository;
import eu.freme.broker.security.database.repository.UserRepository;
import eu.freme.broker.security.tools.AccessLevelHelper;

import eu.freme.conversion.rdf.RDFConstants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 03.09.15.
 */
public class FremeNERTestSecurity extends IntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DatasetDAO datasetDAO;

    String[] availableLanguages = {"en"};//,"de","it","nl","fr","es"};
    String datasetName = "dbpedia";
    String testinput= "Enrich this Content please";

    private static final String usernameWithPermission = "userwithpermission";
    private static final String passwordWithPermission  = "testpassword";
    private static final String usernameWithoutPermission = "userwithoutpermission";
    private static final String passwordWithoutPermission  = "testpassword";
    private static String tokenWithPermission;
    private static String tokenWithOutPermission;
    private static String tokenAdmin;
    private static boolean initialized = false;

    public FremeNERTestSecurity(){super("/e-entity/freme-ner/");}

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
    public void testDatasetManagement() throws UnirestException , IOException {

        if(!initialized)
            initUser();

        String testDataset=readFile("src/test/resources/e-entity/small-dataset-rdfs.nt");
        String testUpdatedDataset=readFile("src/test/resources/e-entity/small-dataset.nt");

        String privateDatasetName = "integration-test-dataset-private";
        String publicDatasetName = "integration-test-dataset-public";

        assertEquals(HttpStatus.CREATED.value(), createDataset(privateDatasetName, testDataset, tokenWithPermission, "private"));
        assertEquals(HttpStatus.CREATED.value(), createDataset(publicDatasetName, testDataset, tokenWithPermission, "public"));

        // User without permission should not be able to query, update or delete another user's template
        // User with permission should
        // check query template...
        assertEquals(HttpStatus.OK.value(), getDataset(privateDatasetName, tokenWithPermission));
        assertEquals(HttpStatus.FORBIDDEN.value(), getDataset(privateDatasetName, tokenWithOutPermission));

        assertEquals(HttpStatus.OK.value(), getDataset(publicDatasetName, tokenWithOutPermission));

        // check update template...
        /*assertEquals(updateDataset(privateDatasetName,testUpdatedDataset, tokenWithOutPermission, "private"),  HttpStatus.FORBIDDEN.value());
        assertEquals(updateDataset(privateDatasetName,testUpdatedDataset, tokenWithPermission, "public"),  HttpStatus.OK.value());
        assertEquals(getDataset(privateDatasetName, tokenWithOutPermission), HttpStatus.OK.value());
        assertEquals(updateDataset(privateDatasetName,testUpdatedDataset, tokenWithPermission, "private"), HttpStatus.OK.value());
        assertEquals(getDataset(privateDatasetName, tokenWithOutPermission), HttpStatus.FORBIDDEN.value());
        */

        // check delete template...
        assertEquals(HttpStatus.FORBIDDEN.value(), deleteDataset(privateDatasetName, tokenWithOutPermission));
        assertEquals(HttpStatus.FORBIDDEN.value(), deleteDataset(publicDatasetName, tokenWithOutPermission));
        //int responseCode = deleteDataset(privateDatasetName, tokenWithPermission);
        //assertTrue(responseCode == HttpStatus.OK.value() || responseCode == HttpStatus.NO_CONTENT.value());
        assertTrue(deleteDataset(privateDatasetName, tokenWithPermission) <= 204);
        assertTrue(deleteDataset(publicDatasetName, tokenWithPermission) <= 204);
    }

    private int createDataset(String name, String dataset, String token, String visibility) throws UnirestException {
        logger.info("check, if dataset with name \"" + name + "\" exists...");
        HttpResponse<String> response= baseRequestGet("datasets/" + name)
                .header("X-Auth-Token", token)
                .asString();
        if(response.getStatus()==HttpStatus.FORBIDDEN.value())
            return response.getStatus();
        if (response.getStatus()!=HttpStatus.OK.value()) {
            logger.info("dataset with name \""+name+"\" does not exist. Create it...");
            response=baseRequestPost("datasets")
                    .header("X-Auth-Token", token)
                    .queryString("informat", "n-triples")
                    .queryString("description","Test-Description")
                    .queryString("language","en")
                    .queryString("name",name)
                    .queryString("visibility",visibility)
                    .body(dataset)
                    .asString();
            //assertTrue(response.getStatus()<=201);
        }
        return response.getStatus();
    }

    private int updateDataset(String name, String dataset, String token, String visibility) throws UnirestException {
        logger.info("update dataset (\"" + name + "\")...");
        HttpResponse<String> response=baseRequestPut("datasets/"+name)
                .header("X-Auth-Token", token)
                .queryString("informat", "n-triples")
                .queryString("language","en")
                .queryString("visibility", visibility)
                .body(dataset).asString();
        System.out.println(response.getBody());
        //assertTrue(response.getStatus() <= 201);
        return response.getStatus();
    }

    private int getDataset(String name, String token) throws UnirestException {
        logger.info("query dataset (\"" + name + "\")...");
        HttpResponse<String> response= baseRequestGet("datasets/" + name)
                .header("X-Auth-Token", token)
                .queryString("outformat", "turtle").asString();
        return response.getStatus();
    }

    private int deleteDataset(String name, String token) throws UnirestException {
        logger.info("delete dataset (\""+name+"\")...");
        HttpResponse<String> response=baseRequestDelete("datasets/" + name)
                .header("X-Auth-Token", token)
                .asString();
        return response.getStatus();
    }

    private int updateDatasetMetadata(String name, String newOwner, String visibility, String token) throws UnirestException {
        logger.info("update dataset metadata (datasetName: " + name + ", newOwner: "+newOwner+", visibility: "+visibility+")...");
        HttpResponse<String> response=baseRequestPut("datasets/admin/" + name)
                .header("X-Auth-Token", token)
                .queryString("owner", newOwner)
                .queryString("visibility", visibility)
                .asString();
        //System.out.println(response.getBody());
        //assertTrue(response.getStatus() <= 201);
        return response.getStatus();
    }

    @Test
    public void TestFremeNER() throws UnirestException, IOException, UnsupportedEncodingException {

        if(!initialized)
            initUser();

        /*
        String testDataset=readFile("src/test/resources/e-entity/small-dataset-rdfs.nt");
        assertEquals(updateDataset("dbpedia",testDataset, tokenWithPermission, "private"), HttpStatus.OK.value());
*/

        String testinputEncoded= URLEncoder.encode(testinput, "UTF-8");
        String data = readFile("src/test/resources/rdftest/e-entity/data.ttl");

        HttpResponse<String> response;

        assertEquals(HttpStatus.UNAUTHORIZED.value(), updateDatasetMetadata(datasetName, usernameWithPermission, null, tokenWithPermission));


        assertEquals(HttpStatus.OK.value(), updateDatasetMetadata(datasetName, usernameWithPermission, "private", tokenAdmin));
        response = baseRequestPost("documents")
                .header("X-Auth-Token", tokenWithOutPermission)
                .queryString("input", testinputEncoded)
                .queryString("language", availableLanguages[0])
                .queryString("informat", "text")
                .queryString("dataset", datasetName)
                .asString();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        // visibility == null defaults tp "PUBLIC"
        assertEquals(HttpStatus.OK.value(), updateDatasetMetadata(datasetName, usernameWithPermission, null, tokenAdmin));
        response = baseRequestPost("documents")
                .header("X-Auth-Token", tokenWithOutPermission)
                .queryString("input", testinputEncoded)
                .queryString("language", availableLanguages[0])
                .queryString("informat", "text")
                .queryString("dataset", datasetName)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());



        //Tests every language
        for (String lang : availableLanguages) {

            //Tests POST
            //Plaintext Input in Query String
            response = baseRequestPost("documents")
                    .header("X-Auth-Token", tokenWithPermission)
                    .queryString("input", testinputEncoded)
                    .queryString("language", lang)
                    .queryString("informat", "text")
                    .queryString("dataset", datasetName)
                    .asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

            //Tests POST
            //Plaintext Input in Body
            response = baseRequestPost("documents")
                    .header("X-Auth-Token", tokenWithPermission)
                    .queryString("language", lang)
                    .queryString("dataset", datasetName)
                    .header("Content-Type", "text/plain")
                    .body(testinput)
                    .asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
            //Tests POST
            //NIF Input in Body (Turtle)
            response = baseRequestPost("documents").header("Content-Type", "text/turtle")
                    .header("X-Auth-Token", tokenWithPermission)
                    .queryString("language", lang)
                    .queryString("dataset", datasetName)
                    .body(data).asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);


            //Tests POST
            //Test Prefix
            response = baseRequestPost("documents")
                    .header("X-Auth-Token", tokenWithPermission)
                    .queryString("input", testinput)
                    .queryString("language", lang)
                    .queryString("dataset", datasetName)
                    .queryString("informat", "text")
                    .queryString("prefix", "http://test-prefix.com")
                    .asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
            //assertTrue(response.getString() contains prefix)

            //Tests GET
            //response = Unirest.get(getUrl() + "documents?informat=text&input=" + testinputEncoded + "&language=" + lang + "&dataset=" + dataset).asString();
            response = baseRequestGet("documents")
                    .header("X-Auth-Token", tokenWithPermission)
                    .queryString("informat", "text")
                    .queryString("dataset", datasetName)
                    .queryString("input", testinputEncoded)
                    .queryString("language", lang)
                    .asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
        }
    }


}
