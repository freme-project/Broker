package eu.freme.broker.integration_tests;

import org.junit.Test;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

/**
 *
 * @author Pieter Heyvaert <pheyvaer.heyvaert@ugent.be>
 */
public class EPublishingTest extends IntegrationTest{

    public EPublishingTest() {
        super("/e-publishing/");
    }

    @Test
    public void testInvalidJSON() throws UnirestException {
//        HttpRequestWithBody baseRequest = baseRequest("html");
//        baseRequest.field("htmlZip", new File("src/test/resources/e-publishing/alice.zip"));
//        baseRequest.field("metadata", "{fdfdff, ffdf : dfdf}");
//        HttpResponse<InputStream> asBinary = baseRequest.asBinary();
//        
//        File f = new File("src/test/resources/e-publishing/alice.zip");
//        System.out.println(f.getAbsolutePath());
//        
//        int status = asBinary.getStatus();
        //Assert.assertEquals(400, status);
    }
}
