package eu.freme.broker.integration_tests;

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



/**
 * Created by jonathan on 28.07.15.
 */
public class FremeNERTest {

    String url = null;
    String[] availableLanguages = {"en","de","it","nl","fr","es"};
    String dataset = "dbpedia";


    @Before
    public void setup(){
        url = IntegrationTestSetup.getURLEndpoint() + "/e-entity/freme-ner/";
    }

    private HttpRequestWithBody baseRequest(String function) {
        return Unirest.post(url + function);
    }

    @Test
    public void TestFremeNER() throws UnirestException, IOException {

        HttpResponse<String> response;
        String data;

        //Tests POST
        for (String lang : availableLanguages) {


            //Plaintext Input in Query String
            response = baseRequest("documents")
                    .queryString("input","Enrich this Content please")
                    .queryString("language",lang)
                    .queryString("dataset",dataset)
                    .queryString("informat","text")
                    .asString();
            assertTrue(response.getStatus() == 200);
            assertTrue(response.getBody().length() > 0);


            //Plaintext Input in Body
            response = baseRequest("documents")
                    .queryString("language", lang)
                    .queryString("dataset",dataset)
                    .header("Content-Type", "text/plain")
                    .body("Enrich this Content please")
                    .asString();
            assertTrue(response.getStatus() == 200);
            assertTrue(response.getBody().length() > 0);

            //NIF Input in Body (Turtle)
            data = readFile("src/test/resources/rdftest/e-translate/data.turtle");
            response = baseRequest("documents").header("Content-Type", "text/turtle")
                    .queryString("dataset",dataset)
                    .queryString("language", lang)
                    .body(data).asString();
            assertTrue(response.getStatus() == 200);
            assertTrue(response.getBody().length() > 0);


            //Test Prefix
            //Plaintext Input in Query String
            response = baseRequest("documents")
                    .queryString("input", "Enrich this Content please")
                    .queryString("language",lang)
                    .queryString("dataset",dataset)
                    .queryString("informat", "text")
                    .queryString("prefix", "http://test-prefix.com")
                    .asString();
            assertTrue(response.getStatus() == 200);
            assertTrue(response.getBody().length() > 0);

            //assertTrue(response.getString() contains prefix)
        }


    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        StringBuilder bldr = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            bldr.append(line);
            bldr.append("\n");
        }
        reader.close();
        return bldr.toString();
    }




}
