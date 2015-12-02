package eu.freme.broker.tools;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.Broker;
import eu.freme.broker.FremeCommonConfig;
import eu.freme.broker.eservices.BaseRestController;
import eu.freme.broker.exception.AccessDeniedException;
import eu.freme.broker.integration_tests.ELinkSecurityTest;
import eu.freme.broker.integration_tests.EServiceTest;
import eu.freme.broker.integration_tests.IntegrationTestConfig;
import eu.freme.broker.integration_tests.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConversionService;
import org.apache.log4j.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 26.10.15.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes =  {Broker.class, FremeCommonConfig.class} )
@Import(Broker.class)
public class BrokerExceptionHandlerTest extends ELinkSecurityTest {

    BrokerExceptionHandler brokerExceptionHandler;

    public BrokerExceptionHandlerTest() throws UnirestException {
        super();
        super.setup();
        brokerExceptionHandler=(BrokerExceptionHandler) IntegrationTestSetup.getContext().getBean(BrokerExceptionHandler.class);
    }


    @Test
    public void testExceptionHandler() throws Throwable {

        Logger logger = Logger.getLogger(BrokerExceptionHandlerTest.class);


        assertNotNull(brokerExceptionHandler);
        brokerExceptionHandler.expect(new AccessDeniedException());

        assertTrue(brokerExceptionHandler.getExpectedException().getClass() == AccessDeniedException.class);




    }
    @Override
    public void testTemplateHandlingWithSecurity(){};

    @Override
    public void testELinkDocuments(){};

    @Override
    public void testUpdateTemplate(){};

    @Override
    public void testGetAllTemplates(){};

    @Override
    public void invalidTemplateId(){};



}
