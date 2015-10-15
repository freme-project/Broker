package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.broker.eservices.feign.FremeNERService;

@RestController
@EnableFeignClients
@Profile("cloudbroker")
public class FremeNERProxy {

	@Autowired
	FremeNERService fremeNerService;

	@RequestMapping(value = "/freme-ner-proxy")
	public String call(@RequestBody String text, @RequestParam String language,
			@RequestParam String outformat, @RequestParam String rdfPrefix) {

		String response = fremeNerService.enrich(text, language, outformat, rdfPrefix);
		return response;
	}
}
