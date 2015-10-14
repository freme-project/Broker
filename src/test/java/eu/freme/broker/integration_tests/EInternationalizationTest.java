/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.broker.integration_tests;

import java.io.IOException;

import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.common.conversion.rdf.RDFConstants;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 06.08.15.
 */
public class EInternationalizationTest extends EServiceTest {

    //Tests E-Internationalization Service used for converting html and xliff to nif format
    // against POST /e-entity/freme-ner/documents
    public EInternationalizationTest(){super("/e-entity/freme-ner/documents");}

    String dataset = "dbpedia";
    String[] sample_xliff = {"test1.xlf"};
    String[] sample_html = {"aa324.html","test10.html","test12.html"};
    String resourcepath= "src/test/resources/e-internationalization/";

    @Test
    public void TestEInternationalization() throws IOException, UnirestException {
        //See EInternationalizationFilter
//        for (String sample_file : sample_xliff) {
//            testContentTypeandInformat("application/x-xliff+xml",readFile(resourcepath+sample_file));
//        }
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
