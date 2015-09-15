package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.conversion.rdf.RDFConstants;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 15.09.2015.
 */
public class UTF8ContentTest extends IntegrationTest{

    String inputSimple = "Hello Berlin";//"Madrid (/məˈdrɪd/, Spanish: [maˈðɾið], locally: [maˈðɾiθ, -ˈðɾi]) is a south-western European city and the capital and largest municipality of Spain.";
    String inputNifTurtle;

    Logger logger = Logger.getLogger(UTF8ContentTest.class);

    public UTF8ContentTest() throws Exception{
        super("");
        inputNifTurtle = readFile("src/test/resources/rdftest/utf8.ttl");
    }

    @Test
    public void testDBPediaSpotlight() throws UnirestException, IOException{
        HttpResponse<String> response;
        logger.info("Test DBPediaSpotlight");
        setService("/e-entity/dbpedia-spotlight/");
        response = baseRequestPost("documents")
                .queryString("language", "en")
                .queryString("informat", "turtle")
                .queryString("confidence", "0.2")
                .body(inputNifTurtle)
                .asString();
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
        logger.info("Test DBPediaSpotlight");
        setService("/e-entity/dbpedia-spotlight/");
        response = baseRequestPost("documents")
                .queryString("language", "en")
                .queryString("informat", "text")
                .queryString("confidence", "0.2")
                .body("Hello Berlin")
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

    @Test
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
    }

    @Test
    public void testTiledETerminology() throws UnirestException, IOException{
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
    }

    @Test
    public void testTiledETranslation() throws UnirestException, IOException{
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
    }

}
