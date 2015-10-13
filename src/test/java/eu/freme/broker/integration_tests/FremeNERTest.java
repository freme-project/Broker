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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.common.conversion.rdf.RDFConstants;


/**
 * Created by jonathan on 28.07.15.
 */
public class FremeNERTest extends EServiceTest {


    String[] availableLanguages = {"en","de","it","nl","fr","es"};
    String dataset = "dbpedia";
    String testinput= "Enrich this Content please";


    public FremeNERTest(){super("/e-entity/freme-ner/");}

    protected HttpRequestWithBody baseRequestPost(String function) {
        return super.baseRequestPost(function)
                .queryString("dataset", dataset);
    }

    protected HttpRequest baseRequestGet(String function) {
        return super.baseRequestGet(function).queryString("dataset", dataset);
    }


    @Test
    public void TestFremeNER() throws UnirestException, IOException, UnsupportedEncodingException {

        HttpResponse<String> response;

        String testinputEncoded= URLEncoder.encode(testinput, "UTF-8");
        String data = readFile("src/test/resources/rdftest/e-entity/data.ttl");

        //Tests every language
        for (String lang : availableLanguages) {

            //Tests POST
            //Plaintext Input in Query String
            response = baseRequestPost("documents")
                    .queryString("input", testinput)
                    .queryString("language", lang)
                    .queryString("informat", "text")
                    .asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

            //Tests POST
            //Plaintext Input in Body
            response = baseRequestPost("documents")
                    .queryString("language", lang)
                    .header("Content-Type", "text/plain")
                    .body(testinput)
                    .asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
            //Tests POST
            //NIF Input in Body (Turtle)
            response = baseRequestPost("documents").header("Content-Type", "text/turtle")
                    .queryString("language", lang)
                    .body(data).asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);


            //Tests POST
            //Test Prefix
            response = baseRequestPost("documents")
                    .queryString("input", testinput)
                    .queryString("language", lang)
                    .queryString("informat", "text")
                    .queryString("prefix", "http://test-prefix.com")
                    .asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
            //assertTrue(response.getString() contains prefix)

            //Tests GET
            response = Unirest.get(getUrl() + "documents?informat=text&input=" + testinputEncoded + "&language=" + lang + "&dataset=" + dataset).asString();
            response = baseRequestGet("documents")
                    .queryString("informat", "text")
                    .queryString("input", testinputEncoded)
                    .queryString("language", lang)
                    .asString();
            validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
        }
    }
}
