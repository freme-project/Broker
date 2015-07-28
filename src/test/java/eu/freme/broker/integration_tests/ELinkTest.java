package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class ELinkTest {


    String url = null;
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

        String path_suffix = "templates";
        //Tests GET e-link/templates/
        response = Unirest.get(url+path_suffix+"/").asString();
        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);
        model = converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE);
        assertNotNull(model);




    }
}
