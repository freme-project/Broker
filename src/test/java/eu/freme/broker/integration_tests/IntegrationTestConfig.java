package eu.freme.broker.integration_tests;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.freme.broker.Broker;

@Import(Broker.class)
@ComponentScan(basePackages={"eu.freme.broker.integration_tests.mockups"})
public class IntegrationTestConfig {

}
