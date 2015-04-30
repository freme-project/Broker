package eu.freme.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Broker {

	public static void main(String[] args) {
		SpringApplication.run(BrokerConfig.class);
	}
}