package eu.freme.broker.integration_tests.ratelimiter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.twelvemonkeys.imageio.util.ReaderFileSuffixFilter;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.broker.exception.TooManyRequestsException;
import eu.freme.broker.integration_tests.EServiceTest;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.Filter;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 25.11.15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FremeCommonConfig.class)
@ActiveProfiles("broker")
public class RateLimitingFilterTest extends EServiceTest {

    public RateLimitingFilterTest() throws UnirestException {
        super("");
        enableAuthenticate();
    }

    HttpResponse<String> response;

    Logger logger = Logger.getLogger(RateLimitingFilterTest.class);

    String testusername="ratelimitertestuser";
    String testpassword="ratelimiterpassword";

    @Test
    public void testRatelimiting() throws UnirestException, IOException {
        logger.info("starting ratelimiter test");

        logger.info("creating User for ratelimiter test");
        createUser(testusername, testpassword);
        logger.info("authenticating this user");
        String ratelimiterToken = authenticateUser(testusername, testpassword);
        logger.info("trying /e-link/templates call as ratelimitertestuser - should work the first time");

        response = baseRequestGet("/e-entity/freme-ner/datasets",ratelimiterToken).asString();
        logger.info(response.getBody());
        assertEquals(HttpStatus.OK.value(),response.getStatus());

        logger.info("trying /e-entity/freme-ner/datasets call as ratelimitertestuser - should not work the second time");
        loggerIgnore("TooManyRequestsException");
        response = baseRequestGet("/e-entity/freme-ner/datasets",ratelimiterToken).asString();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(),response.getStatus());
        loggerUnignore("TooManyRequestsException");

        logger.info("trying /e-translate/tilde test with huge size as ratelimitertestuser - should not work");
        loggerIgnore("TooManyRequestsException");
        response=baseRequestPost("/e-translation/tilde",ratelimiterToken)
                .queryString("informat", "text")
                .queryString("outformat","turtle")
                .queryString("source-lang","en")
                .queryString("target-lang","de")
                .body(readFile("src/test/resources/rdftest/e-translate/data.txt"))
                .asString();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(),response.getStatus());
        loggerUnignore("TooManyRequestsException");

        logger.info("trying anoter call for which there is no rate-limiting, should work");
        response=baseRequestGet("/e-link/templates").asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());





    }




}
