package eu.freme.broker;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Broker {
	
	private static Logger logger = Logger.getLogger(Broker.class);

	public static void main(String[] args) {
		logger.info("Starting FREME");
		SpringApplication.run(BrokerConfig.class);
	}
}