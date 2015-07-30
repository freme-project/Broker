package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.hp.hpl.jena.shared.AssertionFailureException;
import org.json.JSONObject;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.conversion.rdf.*;
import org.nlp2rdf.cli.Validate;


/**
 * Created by jonathan on 28.07.15.
 */
public class ELinkTest extends IntegrationTest {

    public ELinkTest(){
        super("/e-link/");
    }
    private String constructTemplate(String query, String endpoint) {
        query = query.replaceAll("\n","\\\\n");
        return  " {\n" +
                " \"query\":\""+query+"\",\n" +
                " \"endpoint\":\""+endpoint+"\"\n" +
                " }";
    }


    //Tests Creation, fetching, modification and deletion of a template and fetching of all templates
    @Test
    public void testTemplateHandling() throws IOException, UnirestException{
        String templateid = testELinkTemplatesAdd("src/test/resources/rdftest/e-link/sparql1.ttl");
        testELinkTemplatesId(templateid);
        testELinkTemplatesUpdate("src/test/resources/rdftest/e-link/sparql3.ttl", templateid);
        testELinkTemplates();
        testELinkTemplatesDelete(templateid);
    }

    //Tests GET e-link/templates/
    public void testELinkTemplates() throws UnirestException, IOException {
        HttpResponse<String> response;

        response = baseRequestGet("templates/")
                .queryString("outformat", "json-ld").asString();
        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);
        // validate RDF
        try {
            assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.JSON_LD));
        }catch(Exception e){
            throw new AssertionFailureException("RDF validation failed");
        }
        // validate NIF
        //Validate.main(new String[]{"-i", response.getBody()});
    }

    //Tests POST /e-link/documents/
    @Test
    public void testELinkDocuments() throws Exception {

        String id = testELinkTemplatesAdd("src/test/resources/rdftest/e-link/sparql3.ttl");

        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");

        HttpResponse<String> response = baseRequest("documents/")
                .queryString("templateid", id)
                .queryString("informat", "turtle")
                .queryString("outformat", "turtle")
                .body(nifContent)
                .asString();
        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);
        // validate RDF
        assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE));
        // validate NIF
        Validate.main(new String[]{"-i", response.getBody()});

        testELinkTemplatesDelete(id);
    }

    //// HELPER METHODS

    //Tests POST e-link/templates/
    public String testELinkTemplatesAdd(String filename) throws IOException, UnirestException {
        String query = readFile(filename);



        HttpResponse<String> response = baseRequest("templates/")
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate(query, "http://dbpedia.org/sparql/"))
        .asString();
        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);

        JSONObject jsonObj = new JSONObject(response.getBody());

        String id = jsonObj.getString("templateId");
        // check, if id is numerical
        assertTrue(id.matches("\\d+"));

        // validate RDF
        try {
            assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.JSON_LD));
        }catch(Exception e){
            throw new AssertionFailureException("RDF validation failed");
        }

        // validate NIF
        //Validate.main(new String[]{"-i", response.getBody(), "informat", "json-ld"});

        return id;
    }

    //Tests GET e-link/templates/
    public void testELinkTemplatesId(String id) throws UnirestException, IOException {
        HttpResponse<String> response = baseRequestGet("templates/"+id)
                .queryString("outformat", "turtle")
                .asString();
        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);

        // validate RDF
        try {
            assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE));
        }catch(Exception e){
            throw new AssertionFailureException("RDF validation failed");
        }
        // validate NIF
        Validate.main(new String[]{"-i", response.getBody()});
    }

    //Tests PUT e-link/templates/
    public void testELinkTemplatesUpdate(String filename, String id) throws IOException, UnirestException{
        String query = readFile(filename);

        HttpResponse<String> response = baseRequestPut("templates/"+id)
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate(query, "http://dbpedia.org/sparql/"))
                .asString();
        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);

        JSONObject jsonObj = new JSONObject(response.getBody());
        String newid = jsonObj.getString("templateId");
        // check, if id is numerical
        assertTrue(newid.matches("\\d+"));
        assertTrue(id.equals(newid));

        // validate RDF
        try {
            assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.JSON_LD));
        }catch(Exception e){
            throw new AssertionFailureException("RDF validation failed");
        }

    }

    //Tests DELETE e-link/templates/
    public void testELinkTemplatesDelete(String id) throws UnirestException{
        HttpResponse<String> response = baseRequestDelete("templates/" + id).asString();
        assertTrue(response.getStatus() == 200 || response.getStatus() == 204);
    }

}
