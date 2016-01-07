package eu.freme.broker.integration_tests;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.common.conversion.rdf.JenaRDFConversionService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    final String entityHeader = "entity";
    final String propertyIdentifier = "http://www.w3.org/2005/11/its/rdf#taIdentRef";
    final String resourceIdentifier = "http://dbpedia.org/resource/Berlin";

    final String filterSelect = "SELECT ?"+ entityHeader +" WHERE {[] <"+propertyIdentifier+"> ?"+ entityHeader +"}";
    final String filterConstruct = "CONSTRUCT {?s <"+propertyIdentifier+"> ?"+ entityHeader +"} WHERE {?s <"+propertyIdentifier+"> ?"+ entityHeader +"}";

    @Test
    public void testFilterManagement() throws UnirestException {
        logger.info("create filter1");
        HttpResponse<String> response = addAuthentication(post("manage"), getTokenWithPermission())
                .queryString("entityId", "filter1")
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
        response = addAuthentication(post("manage"), getTokenWithPermission())
                .queryString("entityId", "filter2")
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
    public void testFiltering() throws Exception {
        logger.info("create filter1");
        HttpResponse<String> response = addAuthentication(post("manage"), getTokenWithPermission())
                .queryString("entityId", "filter1")
                .body(filterSelect)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("create filter2");
        response = addAuthentication(post("manage"), getTokenWithPermission())
                .queryString("entityId", "filter2")
                .body(filterConstruct)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String nifContent =
                " @prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
                " @prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
                "\n" +
                "<http://127.0.0.1:9995/spotlight#char=0,15>\n" +
                " a                     nif:Context , nif:Sentence , nif:RFC5147String ;\n" +
                " nif:beginIndex        \"0\" ;\n" +
                " nif:endIndex          \"15\" ;\n" +
                " nif:isString          \"This is Berlin.\" ;\n" +
                " nif:referenceContext  <http://127.0.0.1:9995/spotlight#char=0,15> .\n" +
                "\n" +
                "<http://127.0.0.1:9995/spotlight#char=8,14>\n" +
                " a                     nif:Word , nif:RFC5147String ;\n" +
                " nif:anchorOf          \"Berlin\" ;\n" +
                " nif:beginIndex        \"8\" ;\n" +
                " nif:endIndex          \"14\" ;\n" +
                " nif:referenceContext  <http://127.0.0.1:9995/spotlight#char=0,15> ;\n" +
                " <"+propertyIdentifier+">     <"+resourceIdentifier+"> .";


        logger.info("filter nif with filter1(select)");
        response = post("documents/filter1")
                .queryString("informat", RDFConstants.RDFSerialization.TURTLE.contentType())
                .queryString("outformat", RDFConstants.RDFSerialization.JSON.contentType())
                .body(nifContent)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        InputStream stream = new ByteArrayInputStream(response.getBody().getBytes(StandardCharsets.UTF_8));
        ResultSet resultSet = ResultSetFactory.fromJSON(stream);
        // check resultSet content
        assertTrue(resultSet.nextSolution().get(entityHeader).asResource().equals(ResourceFactory.createResource(resourceIdentifier)));
        assertFalse(resultSet.hasNext());

        logger.info("filter nif with filter2(construct)");
        response = post("documents/filter2")
                .queryString("informat", RDFConstants.RDFSerialization.TURTLE.contentType())
                .queryString("outformat", RDFConstants.RDFSerialization.TURTLE.contentType())
                .body(nifContent)
                .asString();
        //check status code
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        RDFConversionService rdfConversionService = new JenaRDFConversionService();
        Model resultModel = rdfConversionService.unserializeRDF(response.getBody(),RDFConstants.RDFSerialization.TURTLE);
        // check, if the result model contains the required triple
        Query askQuery = QueryFactory.create("ASK {[] <"+propertyIdentifier+"> <"+resourceIdentifier+">}");
        QueryExecution qexec = QueryExecutionFactory.create(askQuery, resultModel) ;
        assertTrue(qexec.execAsk());

        // check, if the result model contains no more triples
        Query countQuery = QueryFactory.create("SELECT (COUNT(*) as ?count) WHERE { ?s ?p ?o. }");
        qexec = QueryExecutionFactory.create(countQuery, resultModel) ;
        resultSet = qexec.execSelect();
        assertEquals(1,resultSet.nextSolution().getLiteral("count").getInt());

        logger.info("delete filter1");
        response = addAuthentication(delete("manage/filter1"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        logger.info("delete filter2");
        response = addAuthentication(delete("manage/filter2"), getTokenWithPermission()).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void testFilteringWithELink() throws Exception {

        HttpResponse<String> response = addAuthentication(post("manage"), getTokenWithPermission())
                .queryString("entityId", "filter1")
                .body(filterSelect)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("create filter2");
        response = addAuthentication(post("manage"), getTokenWithPermission())
                .queryString("entityId", "filter2")
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
                null, //use default: mockupEndpointUrl
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

        logger.info("filter with filter1(SELECT) and outformat: xml");
        assertEquals(HttpStatus.OK.value(), doELink(nifContent,templateId,getTokenWithPermission(),"filter1", "xml"));

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