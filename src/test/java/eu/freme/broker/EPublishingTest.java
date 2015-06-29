package eu.freme.broker;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import java.io.File;
import java.io.InputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author Pieter Heyvaert <pheyvaer.heyvaert@ugent.be>
 */
public class EPublishingTest {

    String url = "http://localhost:8080/e-publishing/";
    ConfigurableApplicationContext context;

    @Before
    public void setup() {
        context = SpringApplication.run(FremeFullConfig.class);
    }

    @After
    public void teardown() {
        context.close();
    }

    private HttpRequestWithBody baseRequest(String function) {
        return Unirest.post(url + function);
    }

    @Test
    public void testInvalidJSON() throws UnirestException {
        HttpRequestWithBody baseRequest = baseRequest("html");
        baseRequest.field("htmlZip", new File("src/test/resources/e-publishing/alice.zip"));
        baseRequest.field("metadata", "{fdfdff, ffdf : dfdf}");
        HttpResponse<InputStream> asBinary = baseRequest.asBinary();
        
        File f = new File("src/test/resources/e-publishing/alice.zip");
        System.out.println(f.getAbsolutePath());
        
        int status = asBinary.getStatus();
        //Assert.assertEquals(400, status);
    }
}
