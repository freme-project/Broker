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
    		@RequestParam(value = "language", required = false) String language,
    		@RequestParam(value = "enrichement", required = false) String enrichementType,
    	    @RequestBody(required = false) String postBody);

}
