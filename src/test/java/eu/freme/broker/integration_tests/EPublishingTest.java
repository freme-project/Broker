package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

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
        // EpubCheck epubcheck = new EpubCheck(targetFile);

        // validate() returns true if no errors or warnings are found
        // assertTrue(epubcheck.validate());
    }
}
