package eu.freme.broker.eservices.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("freme-ner")
public interface FremeNERService {

    @RequestMapping(method = RequestMethod.POST, value = "/freme-ner-test")
    public String enrich(
    		@RequestBody String text,
    		@RequestParam String language,
    		@RequestParam String outformat,
    		@RequestParam String rdfPrefix
    );
}
