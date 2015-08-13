package eu.freme.broker.integration_tests;

import com.hp.hpl.jena.shared.AssertionFailureException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne on 29.07.2015.
 */
public abstract class IntegrationTest {

    private String url = null;
    private String service;
    public RDFConversionService converter;
    public Logger logger = Logger.getLogger(IntegrationTest.class);

    public String getBaseURL() {
        return baseURL;
    }

    private String baseURL;


    //String adminUsername;
    //String adminPassword;
    
    public IntegrationTest(String service){
        this.service = service;
    }

    @Before
    public void setup(){
        baseURL = IntegrationTestSetup.getURLEndpoint();
        url = baseURL + service;
        converter = (RDFConversionService)IntegrationTestSetup.getContext().getBean(RDFConversionService.class);
        //ConfigurableApplicationContext context = IntegrationTestSetup.getApplicationContext();
        //adminUsername = context.getEnvironment().getProperty("admin.username");
        //adminPassword = context.getEnvironment().getProperty("admin.password");
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
        StringBuilder bldr = new StringBuilder();
        for (String line: Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8)) {
                bldr.append(line).append('\n');
        }
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

    public static String constructTemplate(String query, String endpoint) {
        query = query.replaceAll("\n","\\\\n");
        return  " {\n" +
                " \"query\":\""+query+"\",\n" +
                " \"endpoint\":\""+endpoint+"\"\n" +
                " }";
    }


}
