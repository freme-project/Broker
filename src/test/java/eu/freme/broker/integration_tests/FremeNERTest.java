package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.broker.integration_tests.helper;

import com.hp.hpl.jena.rdf.model.Model;
import eu.freme.conversion.rdf.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jonathan on 28.07.15.
 */
public class FremeNERTest {

    String url = null;
    String[] availableLanguages = {"en","de","it","nl","fr","es"};
    String dataset = "dbpedia";
    String testinput= "Enrich this Content please";
    String testinputEncoded= URLEncoder.encode(testinput);

    @Before
    public void setup(){
        url = IntegrationTestSetup.getURLEndpoint() + "/e-entity/freme-ner/";
    }

    private HttpRequestWithBody baseRequest( String function) {
        return Unirest.post(url + function);
    }



    
    @Test
    public void TestFremeNER() throws UnirestException, IOException, Exception {
        Model model;
        JenaRDFConversionService converter = new JenaRDFConversionService();

        HttpResponse<String> response;

        String data = helper.readFile("src/test/resources/rdftest/e-entity/data.ttl");

        //Tests every language
        for (String lang : availableLanguages) {

            //Tests POST
            //Plaintext Input in Query String
            response = baseRequest("documents")
                    .queryString("input", testinput)
                    .queryString("language", lang)
                    .queryString("dataset", dataset)
                    .queryString("informat", "text")
                    .asString();
            assertTrue(response.getStatus() == 200);
            assertTrue(response.getBody().length() > 0);


            //Tests POST
            //Plaintext Input in Body
            response = baseRequest("documents")
                    .queryString("language", lang)
                    .queryString("dataset", dataset)
                    .header("Content-Type", "text/plain")
                    .body(testinput)
                    .asString();
            assertTrue(response.getStatus() == 200);
            assertTrue(response.getBody().length() > 0);
            model = converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE);
            assertNotNull(model);

            //Tests POST
            //NIF Input in Body (Turtle)
            response = baseRequest("documents").header("Content-Type", "text/turtle")
                    .queryString("dataset", dataset)
                    .queryString("language", lang)
                    .body(data).asString();
            assertTrue(response.getStatus() == 200);
            assertTrue(response.getBody().length() > 0);
            model = converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE);
            assertNotNull(model);


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
            model = converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE);
            assertNotNull(model);
            //assertTrue(response.getString() contains prefix)

            //Tests GET
            response = Unirest.get(url+"documents?informat=text&input="+testinputEncoded+"&language="+lang+"&dataset="+dataset).asString();
            assertTrue(response.getStatus() == 200);
            assertTrue(response.getBody().length() > 0);
            model = converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE);
            assertNotNull(model);





        }


    }





}
