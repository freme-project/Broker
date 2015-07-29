package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.conversion.rdf.JenaRDFConversionService;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Arne on 29.07.2015.
 */
public abstract class IntegrationTest {

    private String url = null;
    private String service;
    public static JenaRDFConversionService converter = new JenaRDFConversionService();

    public IntegrationTest(String service){
        this.service = service;
    }

    @Before
    public void setup(){
        url = IntegrationTestSetup.getURLEndpoint() + service;
    }

    protected HttpRequestWithBody baseRequest( String function) {
        return Unirest.post(url + function);
    }

    protected HttpRequest baseRequestGet( String function) {
        return Unirest.get(url + function);
    }

    protected HttpRequestWithBody baseRequestDelete( String function) {
        return Unirest.delete(url + function);
    }

    protected HttpRequestWithBody baseRequestPut( String function) {
        return Unirest.put(url + function);
    }



    public static String readFile(String file) throws IOException {
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
