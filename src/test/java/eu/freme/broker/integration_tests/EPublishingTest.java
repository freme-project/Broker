/**
 * Copyright (C) 2015 Felix Sasaki (Felix.Sasaki@dfki.de)
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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Assume;
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
