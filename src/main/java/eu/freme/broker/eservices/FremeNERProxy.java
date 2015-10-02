package eu.freme.broker.eservices;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableFeignClients
@Profile("cloudbroker")
public class FremeNERProxy {

	@RequestMapping(value="/proxy")
	public void call(String msg){
		
	}
}
