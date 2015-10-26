package eu.freme.broker.tools;

import eu.freme.broker.Broker;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.broker.eservices.BaseRestController;
import eu.freme.broker.exception.AccessDeniedException;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 26.10.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes =  {Broker.class, FremeCommonConfig.class} )
public class BrokerExceptionHandlerTest {

    @Autowired
    BrokerExceptionHandler brokerExceptionHandler;

    @Test
    public void testExceptionHandler() throws Throwable {
        Logger logger = Logger.getLogger(BrokerExceptionHandlerTest.class);


        assertNotNull(brokerExceptionHandler);
        brokerExceptionHandler.expect(new AccessDeniedException());

        assertTrue(brokerExceptionHandler.getExpectedException().getClass() == AccessDeniedException.class);
        throw( new  AccessDeniedException("TEST"));
    }


}
