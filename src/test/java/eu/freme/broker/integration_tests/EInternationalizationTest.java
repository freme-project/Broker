package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.conversion.rdf.RDFConstants;
import org.hibernate.annotations.SourceType;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 06.08.15.
 */
public class EInternationalizationTest extends IntegrationTest {

    //Tests E-Internationalization Service used for converting html and xliff to nif format
    // against POST /e-entity/freme-ner/documents
    public EInternationalizationTest(){super("/e-entity/freme-ner/documents");}

    String dataset = "dbpedia";
    String[] sample_xliff = {"test1.xlf"};
    String[] sample_html = {"aa324.html","test10.html","test12.html"};
    String resourcepath= "src/test/resources/e-internationalization/";

    @Test
    public void TestEInternationalization() throws IOException, UnirestException {
        for (String sample_file : sample_xliff) {
            testContentTypeandInformat("application/x-xliff+xml",readFile(resourcepath+sample_file));
        }
        for (String sample_file : sample_html) {
            testContentTypeandInformat("text/html",readFile(resourcepath+sample_file));
        }
    }

    protected HttpRequestWithBody baseRequestPost() {
        return super.baseRequestPost("")
                .queryString("dataset", dataset);
    }

    private void testContentTypeandInformat(String format, String data) throws UnirestException, IOException {
        HttpResponse<String> response;
        //With Content-Type header
        response = baseRequestPost()
                .header("Content-Type", format)
                .queryString("language", "en")
                .body(data)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

        //With informat QueryString
        response = baseRequestPost()
                .queryString("informat", format)
                .queryString("language", "en")
                .body(data)
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

}
