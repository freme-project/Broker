package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import org.junit.Test;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import java.io.*;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pieter Heyvaert <pheyvaer.heyvaert@ugent.be>
 */
public class EPublishingTest extends IntegrationTest{

    public EPublishingTest() {
        super("/e-publishing/");
    }

    @Test
    public void testValidJSON() throws UnirestException, IOException {
        HttpResponse<InputStream> response = Unirest.post(getUrl()+"html")
                .field("htmlZip", new File("src/test/resources/e-publishing/alice.zip"))
                .field("metadata", (Object) readFile("src/test/resources/e-publishing/metadata.json"))
                .asBinary();

        assertTrue(response.getStatus() == 200);


        byte[] buffer = new byte[response.getBody().available()];
        response.getBody().read(buffer);

        File targetFile = new File("target/test-classes/e-publishing/result.epub");
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.flush();
        assertTrue(targetFile.length()>0);
        //TODO: validate epub??
    }

/*
    @Test
    public void testInvalidJSON() throws UnirestException, IOException {
        HttpResponse<InputStream> response = Unirest.post(getUrl() + "html")
                .field("htmlZip", new File("src/test/resources/e-publishing/alice.zip"))
                .field("metadata", (Object) "{\"ffdf\" : \"dfdf\"}")
                .asBinary();

        System.out.println(response.getStatus());
        assertTrue(response.getStatus() == 400);




        //assertTrue(asBinary.getBody(). .length() > 0);
//        File f = new File("src/test/resources/e-publishing/alice.zip");
//        System.out.println(f.getAbsolutePath());
//
//        int status = asBinary.getStatus();
        //Assert.assertEquals(400, status);
    }
    */
}
