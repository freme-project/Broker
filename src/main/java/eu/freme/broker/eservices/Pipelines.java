package eu.freme.broker.eservices;

import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.eservices.pipelines.core.PipelineService;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

/**
 * <p>Copyright 2015 MMLab, UGent</p>
 *
 * @author Gerald Haesendonck
 */
public class Pipelines extends BaseRestController {
	private final Gson gson = new Gson();

	@Autowired
	PipelineService pipelineAPI;

	@RequestMapping(value = "/pipelining/chain", method = RequestMethod.POST)
	public ResponseEntity<String> pipeline(@RequestBody String requests) throws IOException, UnirestException {
		MultiValueMap<String, String> headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "text/plain");
		SerializedRequest[] serializedRequests = gson.fromJson(requests, SerializedRequest[].class);
		return new ResponseEntity<>(pipelineAPI.chain(serializedRequests), headers, HttpStatus.OK);
	}
}
