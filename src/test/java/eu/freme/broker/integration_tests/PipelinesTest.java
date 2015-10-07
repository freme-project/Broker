package eu.freme.broker.integration_tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Gerald Haesendonck
 */
public class PipelinesTest extends IntegrationTest {

	public PipelinesTest() {
		super("/pipelining/");
	}

	@Test
	public void testSpotlight() throws UnirestException {
		//String data = "A court in Libya has sentenced Saif al-Islam Gaddafi, son of deposed leader Col Muammar Gaddafi, and eight others to death over war crimes linked to the 2011 revolution.";
		String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
		SerializedRequest entityRequest = RequestFactory.createEntitySpotlight(data, "en");
		SerializedRequest linkRequest = RequestFactory.createLink("3");	// Geo pos

		sendRequest(HttpStatus.SC_OK, entityRequest, linkRequest);
	}

	/**
	 * Sends the actual pipeline request. It serializes the request objects to JSON and puts this into the body of
	 * the request.
	 * @param expectedResponseCode	The expected HTTP response code. Will be checked against.
	 * @param requests		The serialized requests to send.
	 * @return				The result of the request. This can either be the result of the pipelined requests, or an
	 *                      error response with some explanation what went wrong in the body.
	 * @throws UnirestException
	 */
	private HttpResponse<String> sendRequest(int expectedResponseCode, SerializedRequest... requests) throws UnirestException {
		List<SerializedRequest> serializedRequests = Arrays.asList(requests);
		String body = RequestFactory.toJson(requests);
		System.out.println("request.body = " + body);

		HttpResponse<String> response = baseRequestPost("chain")
				.header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
				.body(new JsonNode(body))
				.asString();

		// print some response info
		System.out.println("response.getStatus() = " + response.getStatus());
		System.out.println("response.getStatusText() = " + response.getStatusText());
		System.out.println("response.contentType = " + response.getHeaders().getFirst("content-type"));
		System.out.println("response.getBody() = " + response.getBody());

		RDFConstants.RDFSerialization responseContentType = RDFConstants.RDFSerialization.fromValue(response.getHeaders().getFirst("content-type"));
		RDFConstants.RDFSerialization accept = getContentTypeOfLastResponse(serializedRequests);
		assertEquals(expectedResponseCode, response.getStatus());
		if (expectedResponseCode / 100 != 2) {
			assertEquals(RDFConstants.RDFSerialization.JSON, responseContentType);
		} else {
			assertEquals(responseContentType, accept);
		}

		return response;
	}

	/**
	 * Helper method that returns the content type of the response of the last request (or: the value of the 'accept'
	 * header of the last request).
	 * @param serializedRequests	The requests that (will) serve as input for the pipelining service.
	 * @return						The content type of the response that the service will return.
	 */
	private static RDFConstants.RDFSerialization getContentTypeOfLastResponse(final List<SerializedRequest> serializedRequests) {
		String contentType = "";
		if (!serializedRequests.isEmpty()) {
			SerializedRequest lastRequest = serializedRequests.get(serializedRequests.size() - 1);
			Map<String, String> headers = lastRequest.getHeaders();
			if (headers.containsKey("accept")) {
				contentType = headers.get("accept");
			} else {
				Map<String, Object> parameters = lastRequest.getParameters();
				if (parameters.containsKey("outformat")) {
					contentType = parameters.get("outformat").toString();
				}
			}
		}
		RDFConstants.RDFSerialization serialization = RDFConstants.RDFSerialization.fromValue(contentType);
		return serialization != null ? serialization : RDFConstants.RDFSerialization.TURTLE;
	}
}
