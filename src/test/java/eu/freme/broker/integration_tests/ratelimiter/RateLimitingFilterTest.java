package eu.freme.broker.integration_tests.ratelimiter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.broker.integration_tests.EServiceTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.Filter;

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

    HttpResponse response;

//    @Test
//    public void testAnonymousUser() {

//    }

    @Autowired
    ApplicationContext context;

    @Test
    public void testLoggedInUser() {
        System.err.println("lel");

    }




}
