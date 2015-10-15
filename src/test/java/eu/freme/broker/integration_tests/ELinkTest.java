package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 14.10.15.
 */
public class ELinkTest extends EServiceTest {



    public ELinkTest(){
        super("/e-link");
    }


    @Ignore
    @Test
    public void TestELinkExplore() throws UnirestException, IOException {


        String rdf_resource = "http://dbpedia.org/resource/Berlin";
        String endpoint = "http://dbpedia.org/sparql";

        HttpResponse<String> response;

        response=baseRequestPost("/explore")
                .header("informat","turtle")
                .header("outformat","turtle")
                .queryString("resource", rdf_resource)
                .queryString("endpoint", endpoint)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

        endpoint = "http://fragments.dbpedia.org/2014/en";
        response=baseRequestPost("/explore")
                .header("informat","turtle")
                .header("outformat","turtle")
                .queryString("endpoint-type","ldf")
                .queryString("resource", rdf_resource)
                .queryString("endpoint", endpoint)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

        rdf_resource ="asdf";
        endpoint = "http://localhost:8000/mockups/sparql";
        response=baseRequestPost("/explore")
                .header("informat","turtle")
                .header("outformat","turtle")
                .queryString("endpoint-type","sparql")
                .queryString("resource", rdf_resource)
                .queryString("endpoint", endpoint)
                .asString();

        validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);



    }

}
