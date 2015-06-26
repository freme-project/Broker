package eu.freme.broker;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Start Broker with all e-Services.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SpringBootApplication
public class Broker {
	
	private static Logger logger = Logger.getLogger(Broker.class);

	public static void main(String[] args) {
		logger.info("Starting FREME");
		SpringApplication.run(FremeFullConfig.class);
	}
}