package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.security.database.DatasetRepository;
import eu.freme.broker.security.database.UserRepository;
import eu.freme.broker.security.tools.AccessLevelHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 03.09.15.
 */
public class FremeNERTestSecurity extends IntegrationTest {


    @Autowired
    AbstractAccessDecisionManager decisionManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccessLevelHelper accessLevelHelper;

    @Autowired
    DatasetRepository datasetRepository;

    String usernameWithPermission = "userwithpermission";
    String passwordWithPermission  = "testpassword";
    String usernameWithoutPermission = "userwithoutpermission";
    String passwordWithoutPermission  = "testpassword";


    public FremeNERTestSecurity() throws UnirestException {
        super("/e-entity/");
    }

    @Test
    public void testDatasetHandlingWithSecurity() throws Exception{

        //Creates two users, one intended to have permission, the other not
        createUser(usernameWithPermission, passwordWithPermission);
        String tokenWithPermission = authenticateUser(usernameWithPermission, passwordWithPermission);
        createUser(usernameWithoutPermission, passwordWithoutPermission);
        String tokenWithOutPermission = authenticateUser(usernameWithoutPermission, passwordWithoutPermission);


        String testDataset=readFile("src/test/resources/e-entity/small-dataset-rdfs.nt");
        String testUpdatedDataset=readFile("src/test/resources/e-entity/small-dataset.nt");

        String testDatasetName = "integration-test-dataset";


        HttpResponse<String> response= baseRequestGet("datasets/"+testDatasetName).asString();
        if (response.getStatus()!=200) {

            response=baseRequestPost("datasets")
                    .header("X-Auth-Token",tokenWithPermission)
                    .queryString("informat", "n-triples")
                    .queryString("description","Test-Description")
                    .queryString("language","en")
                    .queryString("name",testDatasetName)
                    .body(testDataset)
                    .asString();
            assertTrue(response.getStatus()<=201);
        }
        response= baseRequestGet("datasets/"+testDatasetName)
                .queryString("outformat","turtle").asString();
        assertTrue(response.getStatus()==200);
        /*
        TODO:Fix PUT e-entity/datasets/{dataset-name}
        response=baseRequestPut("datasets/"+testDatasetName)

                    .header("X-Auth-Token",tokenWithPermission)
                .queryString("informat","n-triples")
                .queryString("language","en")
                .body(testUpdatedDataset).asString();

        System.out.println(response.getStatus());
        System.out.println(response.getBody());
        */

        response=baseRequestDelete("datasets/" + testDatasetName)
                .header("X-Auth-Token", tokenWithOutPermission).asString();
        assertTrue(response.getStatus() != 200);

        response=baseRequestDelete("datasets/" + testDatasetName)
                .header("X-Auth-Token",tokenWithPermission).asString();
        assertTrue(response.getStatus() == 200);

    }


}
