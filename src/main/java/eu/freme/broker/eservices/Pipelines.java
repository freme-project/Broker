package eu.freme.broker.eservices;

import com.google.gson.JsonSyntaxException;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.exception.NotAcceptableException;
import eu.freme.eservices.pipelines.core.PipelineService;
import eu.freme.eservices.pipelines.core.ServiceException;
import eu.freme.eservices.pipelines.requests.RequestBuilder;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import eu.freme.eservices.pipelines.core.PipelineResponse;

import java.util.List;

/**
 * @author Gerald Haesendonck
 */
@RestController
@SuppressWarnings("unused")
public class Pipelines extends BaseRestController {

	@Autowired
	PipelineService pipelineAPI;

	/**
	 * <p>Calls the pipelining service.</p>
	 * <p>Some predefined Requests can be formed using the class {@link RequestFactory}. It also converts request objects
	 * from and to JSON.</p>
	 * <p><To create custom requests, use the {@link RequestBuilder}.</p>
	 * <p>Examples can be found in the unit tests in the Pipelines repository.</p>
	 * @param requests	The requests to send to the service.
	 * @return          The response of the last request.
	 * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
	 */
	@RequestMapping(value = "/pipelining/chain",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = {"text/turtle", "application/json", "application/ld+json", "application/n-triples", "application/rdf+xml", "text/n3"}
	)
	public ResponseEntity<String> pipeline(@RequestBody String requests) {
		try {
			List<SerializedRequest> serializedRequests = RequestFactory.fromJson(requests);
			PipelineResponse pipelineResult = pipelineAPI.chain(serializedRequests);
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, pipelineResult.getContentType());
			return new ResponseEntity<>(pipelineResult.getBody(), headers, HttpStatus.OK);
		} catch (ServiceException serviceError) {
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, serviceError.getResponse().getContentType());
			return new ResponseEntity<>(serviceError.getMessage(), headers, serviceError.getStatus());
		} catch (JsonSyntaxException jsonException) {
			String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
			throw new NotAcceptableException("Error detected in the JSON body contents: " + errormsg);
		} catch (Throwable t) {
			// throw a FREME exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}
}