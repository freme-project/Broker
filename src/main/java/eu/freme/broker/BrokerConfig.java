package eu.freme.broker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.freme.conversion.ConversionApplicationConfig;

@SpringBootApplication
@ComponentScan("eu.freme.broker.eservices")
@Import(ConversionApplicationConfig.class)
public class BrokerConfig {
}