package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.FremeCommonConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 15.12.2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FremeCommonConfig.class)
@ActiveProfiles("broker")
public class FilterControllerTest extends EServiceTest {

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
}