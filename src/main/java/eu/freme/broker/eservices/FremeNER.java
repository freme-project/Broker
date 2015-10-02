package eu.freme.broker.eservices;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.broker.eservices.feign.FremeNERService;

@RestController
@Profile("fremener")
public class FremeNER implements FremeNERService{

    @RequestMapping(method = RequestMethod.POST, value = "/freme-ner-test")
    public String enrich(@RequestBody String body){	
    	return body.toUpperCase();
    }
}
