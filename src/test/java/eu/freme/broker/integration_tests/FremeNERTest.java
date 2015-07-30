package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.hp.hpl.jena.shared.AssertionFailureException;
import com.mashape.unirest.request.HttpRequest;
import eu.freme.conversion.rdf.*;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.nlp2rdf.cli.Validate;


/**
 * Created by jonathan on 28.07.15.
 */
public class FremeNERTest extends IntegrationTest{


    String[] availableLanguages = {"en","de","it","nl","fr","es"};
    String dataset = "dbpedia";
    String testinput= "Enrich this Content please";


    public FremeNERTest(){super("/e-entity/freme-ner/");}

    protected HttpRequestWithBody baseRequest(String function) {
        return super.baseRequest(function)
                .queryString("dataset", dataset);
    }

    protected HttpRequest baseRequestGet(String function) {
        return super.baseRequestGet(function).queryString("dataset", dataset);
    }


    @Test
    public void TestFremeNER() throws UnirestException, IOException, UnsupportedEncodingException {

        HttpResponse<String> response;

        String testinputEncoded= URLEncoder.encode(testinput, "UTF-8");
        String data = readFile("src/test/resources/rdftest/e-entity/data.ttl");

        //Tests every language
        for (String lang : availableLanguages) {

            //Tests POST
            //Plaintext Input in Query String
            response = baseRequest("documents")
                    .queryString("input", testinput)
                    .queryString("language", lang)
                    .queryString("informat", "text")
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

            //Tests POST
            //Plaintext Input in Body
            response = baseRequest("documents")
                    .queryString("language", lang)
                    .header("Content-Type", "text/plain")
                    .body(testinput)
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

            //Tests POST
            //NIF Input in Body (Turtle)
            response = baseRequest("documents").header("Content-Type", "text/turtle")
                    .queryString("language", lang)
                    .body(data).asString();
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


            //Tests POST
            //Test Prefix
            response = baseRequest("documents")
                    .queryString("input", testinput)
                    .queryString("language", lang)
                    .queryString("dataset", dataset)
                    .queryString("informat", "text")
                    .queryString("prefix", "http://test-prefix.com")
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

            //assertTrue(response.getString() contains prefix)

            //Tests GET
            //response = Unirest.get(url+"documents?informat=text&input="+testinputEncoded+"&language="+lang+"&dataset="+dataset).asString();
            response = baseRequestGet("documents")
                    .queryString("informat", "text")
                    .queryString("input", testinputEncoded)
                    .queryString("language", lang)
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
    }
}
