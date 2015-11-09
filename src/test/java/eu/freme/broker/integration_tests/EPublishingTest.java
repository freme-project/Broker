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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.adobe.epubcheck.api.EpubCheck;
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
public class EPublishingTest extends EServiceTest {

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

        // simple constructor; errors are printed on stderr stream
        EpubCheck epubcheck = new EpubCheck(targetFile);

        // validate() returns true if no errors or warnings are found
        logger.info("validate epub file");
        assertTrue(epubcheck.validate());
    }
}
