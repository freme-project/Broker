package eu.freme.broker;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.ComponentScan.Filter;

import eu.freme.broker.tools.StarterHelper;

//@ComponentScan(basePackages = "eu.freme.broker", excludeFilters = @Filter(type = FilterType.REGEX, pattern = { "eu.freme.broker.security.*" }))
@Profile("cloud-freme-ner")
@EnableDiscoveryClient
//@EnableAutoConfiguration(exclude = { org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class, })
@Import(FremeNERStarter.class)
/**
 * Start FREME ner as part of FREME cloud
 * 
 * @author Jan Nehring- jan.nehring@dfki.de
 *
 */
public class CloudFremeNER {
	static Logger logger = Logger.getLogger(Broker.class);

	public static void main(String[] args) {
		logger.info("Starting FREME NER in cloud mode");
		String[] newArgs = StarterHelper.addProfile(args,
				"fremener,cloud-freme-ner");
		SpringApplication.run(CloudFremeNER.class, newArgs);
	}
}
