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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;

import com.hp.hpl.jena.shared.AssertionFailureException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;

/**
 * Created by Arne on 29.07.2015.
 */
public abstract class IntegrationTest {

    private String url = null;
    private String service;
    public RDFConversionService converter;
    
    public IntegrationTest(String service){
        this.service = service;
    }

    @Before
    public void setup(){

        url = IntegrationTestSetup.getURLEndpoint() + service;
        converter = (RDFConversionService)IntegrationTestSetup.getContext().getBean(RDFConversionService.class);
    }

    //All HTTP Methods used in FREME are defined.
    protected HttpRequestWithBody baseRequestPost(String function) {
        return Unirest.post(url + function);
    }
    protected HttpRequest baseRequestGet( String function) {
        return Unirest.get(url + function);
    }
    protected HttpRequestWithBody baseRequestDelete( String function) {
        return Unirest.delete(url + function);
    }
    protected HttpRequestWithBody baseRequestPut( String function) {
        return Unirest.put(url + function);
    }

    //Simple getter which returns the url to the endpoint
    public String getUrl() {
        return url;
    }


    //Reads a text file line by line. Use this when testing API with examples from /test/resources/
    public static String readFile(String file) throws IOException {
        StringBuilder bldr = new StringBuilder();
        for (String line: Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8)) {
                bldr.append(line).append('\n');
        }
        return bldr.toString();
    }

    //General NIF Validation: can be used to test all NiF Responses on their validity.
    public void validateNIFResponse(HttpResponse<String> response, RDFConstants.RDFSerialization nifformat) throws IOException {

        //basic tests on response
        assertTrue(response.getStatus() == 200);
        assertTrue(response.getBody().length() > 0);
        assertTrue(!response.getHeaders().isEmpty());
        assertNotNull(response.getHeaders().get("content-type"));

        //Tests if headers are correct.
        String contentType = response.getHeaders().get("content-type").get(0).split(";")[0];
        //TODO: Special Case due to WRONG Mime Type application/json+ld.
        // We wait for https://github.com/freme-project/technical-discussion/issues/40
        if (contentType.equals("application/ld+json") && nifformat.contentType().equals("application/json+ld")) {
        } else {
            assertTrue(contentType.equals(nifformat.contentType()));
        }

        // validate RDF
        try {
            assertNotNull(converter.unserializeRDF(response.getBody(), nifformat));
        } catch (Exception e) {
            throw new AssertionFailureException("RDF validation failed");
        }



        // validate NIF
        /* the Validate modul is available just as SNAPSHOT.
        if (nifformat == RDFConstants.RDFSerialization.TURTLE) {
            Validate.main(new String[]{"-i", response.getBody(), "--informat","turtle"});
        } else if (nifformat == RDFConstants.RDFSerialization.RDF_XML) {
            Validate.main(new String[]{"-i", response.getBody(), "--informat","rdfxml"});
        } else {
            //Not implemented yet: n3, n-triples, json-ld
            Validate.main(new String[]{"-i", response.getBody()});
        }
        */

    }


}
