package eu.freme.broker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.freme.conversion.ConversionApplicationConfig;
import eu.freme.eservices.eentity.EEntityConfig;
import eu.freme.eservices.elink.ELinkConfig;

@SpringBootApplication
@ComponentScan("eu.freme.broker.eservices")
@Import( {ConversionApplicationConfig.class, EEntityConfig.class})
public class BrokerConfig {
}