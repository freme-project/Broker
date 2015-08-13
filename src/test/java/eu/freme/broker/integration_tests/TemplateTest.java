package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import eu.freme.broker.security.database.TemplateRepository;
import eu.freme.broker.security.database.UserRepository;
import eu.freme.broker.security.tools.AccessLevelHelper;
import eu.freme.conversion.rdf.RDFConstants;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;

import static org.junit.Assert.assertTrue;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 13.08.15.
 */
public class TemplateTest extends IntegrationTest{
    @Autowired
    AbstractAccessDecisionManager decisionManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccessLevelHelper accessLevelHelper;

    @Autowired
    TemplateRepository templateRepository;

    Logger logger = Logger.getLogger(UserControllerTest.class);


    public TemplateTest(){
        super("/e-link/");
    }

    //Tests POST e-link/templates/
    public String testELinkTemplatesAddSecurity(String filename) throws Exception {
        String query = readFile(filename);



        HttpResponse<String> response = baseRequestPost("templates")
                .queryString("informat", "json")
                .queryString("outformat", "json-ld")
                .body(constructTemplate(query, "http://dbpedia.org/sparql/"))
                .asString();
        validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);

        JSONObject jsonObj = new JSONObject(response.getBody());

        String id = jsonObj.getString("templateId");
        // check, if id is numerical
        assertTrue(id.matches("\\d+"));

        return id;
    }

}
