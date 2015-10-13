package eu.freme.broker;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.freme.broker.tools.StarterHelper;

@SpringCloudApplication
@Profile("cloudbroker")
@EnableDiscoveryClient
@Import(Broker.class)
@EnableFeignClients

/**
 * Start broker as part of FREME cloud
 * 
 * @author Jan Nehring- jan.nehring@dfki.de
 *
 */
public class CloudBroker {
	static Logger logger = Logger.getLogger(Broker.class);

	public static void main(String[] args){
		logger.info("Starting FREME in cloud broker mode");
		String[] newArgs = StarterHelper.addProfile(args, "broker,cloudbroker");
		SpringApplication.run(CloudBroker.class, newArgs);
	}
}
