/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * 					Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * 					Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONObject;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.conversion.rdf.RDFConstants;


/**
 * Created by Jonathan Sauder (jsauder@campus-tu-berlin.de) on 28.07.15.
 */
public class ELinkTest extends IntegrationTest {
    String label = "My Label";
    String endpoint = "http://dbpedia.org/sparql/";
    String description = "My description for this tenmplate";

    public ELinkTest(){
        super("/e-link/");
    }
    private String constructTemplate(String label, String query, String endpoint, String description) {
        query = query.replaceAll("\n","\\\\n");
        return  " {\n" +
                "\"label\":\""+ label + "\",\n"+
                " \"query\":\""+query+"\",\n" +
                " \"endpoint\":\""+endpoint+"\",\n" +
                "\"description\":\""+ description + "\"\n"+
                " }";
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
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate(label, query, endpoint, description))
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
                .body(constructTemplate(label, query, endpoint, description))
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
