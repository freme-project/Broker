package eu.freme.broker.eservices;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.conversion.rdf.RDFConstants;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author Gerald Haesendonck
 */
@RestController
@SuppressWarnings("unused")
public class Pipelines {

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
	 * @throws IOException
	 * @throws UnirestException
	 */
	@RequestMapping(value = "/pipelining/chain", method = RequestMethod.POST)
	public ResponseEntity<String> pipeline(@RequestBody String requests) throws IOException, UnirestException {
		List<SerializedRequest> serializedRequests = RequestFactory.fromJson(requests);
		try {
			String pipelineResult = pipelineAPI.chain(serializedRequests);
			RDFConstants.RDFSerialization returnedContentType = PipelineService.getContentTypeOfLastResponse(serializedRequests);
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, returnedContentType.contentType());
			return new ResponseEntity<>(pipelineResult, headers, HttpStatus.OK);
		} catch (ServiceException serviceError) {
			return new ResponseEntity<>(serviceError.getMessage(), serviceError.getStatus());
		}
	}
}