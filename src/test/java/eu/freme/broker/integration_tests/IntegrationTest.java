package eu.freme.broker.integration_tests;

import com.hp.hpl.jena.shared.AssertionFailureException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne on 29.07.2015.
 */
public abstract class IntegrationTest {

    private String url = null;
    private String service;
    public RDFConversionService converter;
    
    public IntegrationTest(String service){
        this.service = service;
    }

    @Before
    public void setup(){
        url = IntegrationTestSetup.getURLEndpoint() + service;
        converter = (RDFConversionService)IntegrationTestSetup.getContext().getBean(RDFConversionService.class);
    }

    protected HttpRequestWithBody baseRequestPost(String function) {
        return Unirest.post(url + function);
    }

    public String getUrl() {
        return url;
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

    public void validateNIFResponse(HttpResponse<String> response, RDFConstants.RDFSerialization nifformat) throws IOException {

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);

        assertTrue(!response.getHeaders().isEmpty());
        assertNotNull(response.getHeaders().get("content-type"));
        String contentType = response.getHeaders().get("content-type").get(0).split(";")[0];
        //TODO: Special Case due to WRONG Mime Type application/json+ld
        if (contentType.equals("application/ld+json") && nifformat.contentType().equals("application/json+ld")) {
        } else {
            assertTrue(contentType.equals(nifformat.contentType()));
        }

        // validate RDF
        try {
            assertNotNull(converter.unserializeRDF(response.getBody(), nifformat));
        } catch (Exception e) {
            throw new AssertionFailureException("RDF validation failed");
        }
        // validate NIF
        /* the Validate modul is available just as SNAPSHOT
        if (nifformat == RDFConstants.RDFSerialization.TURTLE) {
            Validate.main(new String[]{"-i", response.getBody(), "--informat","turtle"});
        } else if (nifformat == RDFConstants.RDFSerialization.RDF_XML) {
            Validate.main(new String[]{"-i", response.getBody(), "--informat","rdfxml"});
        } else {
            //Not implemented yet: n3, n-triples, json-ld
//            Validate.main(new String[]{"-i", response.getBody()});
        }
        */

    }


}