package eu.freme.broker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.freme.eservices.eentity.EEntityConfig;
import eu.freme.eservices.elink.ELinkConfig;
import eu.freme.eservices.epublishing.EPublishingConfig;

/**
 * loads BrokerConfig + API endpoints
 * 
 * @author jnehring
 */
@SpringBootApplication
@ComponentScan(basePackages="eu.freme.broker.eservices")
@Import({BrokerConfig.class, EEntityConfig.class, ELinkConfig.class, EPublishingConfig.class})
public class FremeFullConfig {

}
