package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.conversion.rdf.RDFConstants;
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

    protected HttpRequestWithBody baseRequestPost() {
        return super.baseRequestPost("")
                .queryString("dataset", dataset);
    }

    @Test
    public void TestEInternationalization() throws IOException, UnirestException {
        HttpResponse<String> response;
        String data;

        //Tests text/html Input
        data = readFile("src/test/resources/e-internationalization/aa324.html");

        //With Content-Type header
        response = baseRequestPost()
                .header("Content-Type", "text/html")
                .queryString("language", "en")
                .body(data)
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

        //With informat QueryString
        response = baseRequestPost()
                .queryString("informat", "text/html")
                .queryString("language", "en")
                .body(data)
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);


        //Tests application/x-xliff+xml
        data = readFile("src/test/resources/e-internationalization/test1.xlf");

        //With Content-Type header
        response = baseRequestPost()
                .header("Content-Type", "application/x-xliff+xml")
                .queryString("language", "en")
                .body(data)
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

        //With informat QueryString
        response = baseRequestPost()
                .queryString("informat", "application/x-xliff+xml")
                .queryString("language", "en")
                .body(data)
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

}
