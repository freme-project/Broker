package eu.freme.broker.integration_tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 15.12.2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FremeCommonConfig.class)
@ActiveProfiles("broker")
public class FilterControllerTest extends EServiceTest {

    ELinkSecurityTest eLinkSecurityTest = new ELinkSecurityTest();

    public FilterControllerTest() throws UnirestException {
        super("/toolbox/filter/");
        enableAuthenticate();
    }

    final String filterSelect =
                    "PREFIX itsrdf: <http://www.w3.org/2005/11/its/rdf#>\n" +
                    "SELECT ?s ?o\n" +
                    "WHERE {?s itsrdf:taIdentRef ?o}";
    final String filterConstruct =
                    "PREFIX itsrdf: <http://www.w3.org/2005/11/its/rdf#>\n" +
                    "CONSTRUCT {?s itsrdf:taIdentRef ?o} WHERE {?s itsrdf:taIdentRef ?o}";

    @Test
    public void testFilterManagement() throws UnirestException {
        logger.info("create filter1");
        HttpResponse<String> response = addAuthentication(post("manage/filter1"), getTokenWithPermission())
                .body(filterSelect)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("get filter1");
        response = addAuthentication(get("manage/filter1"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        JSONObject json = new JSONObject(response.getBody());
        assertEquals(filterSelect, json.getString("query"));

        logger.info("update filter1");
        response = addAuthentication(put("manage/filter1"), getTokenWithPermission())
                .body(filterConstruct)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("get updated filter1");
        response = addAuthentication(get("manage/filter1"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        json = new JSONObject(response.getBody());
        assertEquals(filterConstruct, json.getString("query"));

        logger.info("create filter2");
        response = addAuthentication(post("manage/filter2"), getTokenWithPermission())
                .body(filterSelect)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("get all filters");
        response = addAuthentication(get("manage"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        JSONArray jsonArray = new JSONArray(response.getBody());
        assertTrue((filterConstruct.equals(((JSONObject)jsonArray.get(0)).getString("query")) && filterSelect.equals(((JSONObject)jsonArray.get(1)).getString("query")))
            || (filterConstruct.equals(((JSONObject)jsonArray.get(1)).getString("query")) && filterSelect.equals(((JSONObject)jsonArray.get(0)).getString("query"))));

        logger.info("delete filter1");
        response = addAuthentication(delete("manage/filter1"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        logger.info("delete filter2");
        response = addAuthentication(delete("manage/filter2"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

    }

    @Test
    public void testFilteringWithELink() throws Exception {


        logger.info("create filter1");
        HttpResponse<String> response = addAuthentication(post("manage/filter1"), getTokenWithPermission())
                .body(filterSelect)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("create filter2");
        response = addAuthentication(post("manage/filter2"), getTokenWithPermission())
                .body(filterConstruct)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("initialize elink-test");
        eLinkSecurityTest.setup();
        eLinkSecurityTest.replaceBaseUrl();

        logger.info("create template");
        long templateId = eLinkSecurityTest.createTemplate(eLinkSecurityTest.constructTemplate(
                "Find nearest museums",
                "PREFIX dbpedia: <http://dbpedia.org/resource/> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> CONSTRUCT {  ?museum <http://xmlns.com/foaf/0.1/based_near> <@@@entity_uri@@@> . } WHERE {  <@@@entity_uri@@@> geo:geometry ?citygeo .  OPTIONAL { ?museum rdf:type dbo:Museum . }  ?museum geo:geometry ?museumgeo .  FILTER (<bif:st_intersects>(?museumgeo, ?citygeo, 50)) } LIMIT 10",
                null,
                "This template enriches with a list of museums (max 10) within a 50km radius around each location entity.",
                "SPARQL",
                "public"),
                getTokenWithPermission());


        logger.info("read nif to enrich");
        String nifContent = readFile("src/test/resources/rdftest/e-link/data.ttl");

        logger.info("filter with filter1(SELECT) and outformat: csv");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent,templateId,getTokenWithPermission(),"filter1", "csv"));

        logger.info("filter with filter1(SELECT) and outformat: json");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent,templateId,getTokenWithPermission(),"filter1", "json"));

        //TOSO: fix this!
        //logger.info("filter with filter1(SELECT) and outformat: xml");
        //assertEquals(HttpStatus.OK.value(), doELink(nifContent,templateId,getTokenWithPermission(),"filter1", "xml"));

        logger.info("filter with filter2(CONSTRUCT) and outformat: turtle");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent,templateId,getTokenWithPermission(),"filter2", "turtle"));

        eLinkSecurityTest.deleteTemplate(templateId, getTokenWithPermission());

        logger.info("delete filter1");
        response = addAuthentication(delete("manage/filter1"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        logger.info("delete filter2");
        response = addAuthentication(delete("manage/filter2"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    private int doELink(String nifContent, long templateId, String token, String filterName, String outformat) throws UnirestException, IOException {
        HttpResponse<String> response = addAuthentication(eLinkSecurityTest.post("documents"), token)
                .queryString("templateid", templateId)
                .queryString("informat", "turtle")
                .queryString("outformat", outformat)
                .queryString("filter", filterName)
                .body(nifContent)
                .asString();
        return response.getStatus();
    }
}