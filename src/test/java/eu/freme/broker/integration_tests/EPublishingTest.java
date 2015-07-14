package eu.freme.broker.integration_tests;

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
public class EPublishingTest extends IntegrationTestBase{

	String url = null;
	
	@Before
	public void setup(){
		url = getURLEndpoint() + "/e-publishing/";
	}

    private HttpRequestWithBody baseRequest(String function) {
        return Unirest.post(url + function);
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
