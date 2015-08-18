package eu.freme.broker.integration_tests;

import com.adobe.epubcheck.api.EpubCheck;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

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
    @Ignore
    public void testValidJSON() throws UnirestException, IOException {

        // TODO: wait for issue: Unit tests on windows #15 https://github.com/freme-project/e-Publishing/issues/15
        // Avoid java.io.IOException: Unable to delete temporary files on windows machines
        Assume.assumeTrue(!System.getProperty( "os.name" ).startsWith( "Windows" ));

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
        //File epubFile = new File("/path/to/your/epub/file.epub");

        // simple constructor; errors are printed on stderr stream
        EpubCheck epubcheck = new EpubCheck(targetFile);

        // validate() returns true if no errors or warnings are found
        assertTrue(epubcheck.validate());
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
