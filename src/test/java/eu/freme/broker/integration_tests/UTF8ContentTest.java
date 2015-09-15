package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.conversion.rdf.RDFConstants;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 15.09.2015.
 */
@Ignore
public class UTF8ContentTest extends IntegrationTest{

    String inputSimple = "Madrid (/məˈdrɪd/, Spanish: [maˈðɾið], locally: [maˈðɾiθ, -ˈðɾi]) is a south-western European city and the capital and largest municipality of Spain.";
    String inputNifTurtle;

    Logger logger = Logger.getLogger(UTF8ContentTest.class);

    public UTF8ContentTest() throws Exception{
        super("");
        SimpleLayout layout = new SimpleLayout();
        FileAppender fileAppender = new FileAppender( layout, "logs/utf8-integration-test.log", false );
        fileAppender.setEncoding("UTF-8");
        fileAppender.activateOptions();
        logger.addAppender(fileAppender);


        inputNifTurtle = readFile("src/test/resources/rdftest/utf8.ttl");

        logger.info("\n"+inputNifTurtle);
    }

    @Test
    public void testDBPediaSpotlight() throws UnirestException, IOException{
        HttpResponse<String> response;
        logger.info("Test DBPediaSpotlight");
        setService("/e-entity/dbpedia-spotlight/");
        response = baseRequestPost("documents")
                .queryString("language", "en")
                .queryString("informat", "turtle")
                .body(inputNifTurtle)
                .asString();
        logger.info("\n"+response.getBody());
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

    /*
    @Test
    public void testEInternationalization() throws UnirestException, IOException{
        HttpResponse<String> response;
        logger.info("Test e-Internalization");
        setService("/e-entity/freme-ner/documents");
        response = //
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }
    */

    @Test
    public void testELink() throws Exception{
        logger.info("Test e-Link");
        setService("/e-link/");
        ELinkTest eLinkTest = new ELinkTest();
        String id = eLinkTest.testELinkTemplatesAdd("src/test/resources/rdftest/e-link/sparql3.ttl");

        //String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");

        HttpResponse<String> response = eLinkTest.baseRequestPost("documents")
                .queryString("templateid", id)
                .queryString("informat", "turtle")
                .queryString("outformat", "turtle")
                .body(inputNifTurtle)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
        //Deletes temporary template
        eLinkTest.testELinkTemplatesDelete(id);
    }

    @Test
    public void testFremeNer() throws UnirestException, IOException{
        HttpResponse<String> response;
        logger.info("Test e-entity/freme-ner/");
        setService("/e-entity/freme-ner/");
        response = baseRequestPost("documents")
                .queryString("language", "en")
                .queryString("informat", "turtle")
                .queryString("dataset", "dbpedia")
                .body(inputNifTurtle)
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

    /*@Test
    public void testEPublishing() throws UnirestException, IOException{
        HttpResponse<String> response;
        logger.info("Test DBPediaSpotlight");
        setService("/e-entity/dbpedia-spotlight/");
        response = baseRequestPost("documents")
                .queryString("language", "en")
                .queryString("informat", "text")
                .queryString("confidence", "0.2")
                .body("Hello Berlin")
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }*/

    @Test
    public void testTiledETerminology() throws UnirestException, IOException{
        HttpResponse<String> response;
        logger.info("Test /e-terminology/tilde");
        setService("/e-terminology/tilde");
        response = baseRequestPost("")
                .queryString("informat", "turtle")
                .queryString("source-lang", "en")
                .queryString("target-lang", "de")
                .body(inputNifTurtle)
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

    @Test
    public void testTiledETranslation() throws UnirestException, IOException{
        HttpResponse<String> response;
        logger.info("Test /e-translation/tilde");
        setService("/e-translation/tilde");
        response = baseRequestPost("")
                .queryString("informat", "turtle")
                .queryString("source-lang", "en")
                .queryString("target-lang", "de")
                .body(inputNifTurtle)
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

}
