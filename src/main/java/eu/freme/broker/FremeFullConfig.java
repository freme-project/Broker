package eu.freme.broker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.freme.eservices.eentity.EEntityConfig;
import eu.freme.eservices.elink.ELinkConfig;

/**
 * loads BrokerConfig + API endpoints
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SpringBootApplication
@ComponentScan("eu.freme.broker.eservices")
@Import({BrokerConfig.class, EEntityConfig.class, ELinkConfig.class})
public class FremeFullConfig {

}
