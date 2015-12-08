package eu.freme.broker.integration_tests.pipelines;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import eu.freme.eservices.pipelines.serialization.Serializer;
import org.junit.Test;

/**
 * <p>Tests functionality of wrapping a response with time related statistics.</p>
 * <p>
 * <p>Copyright 2015 MMLab, UGent</p>
 *
 * @author Gerald Haesendonck
 */
public class WrappedResponseTest extends PipelinesCommon {

	@Test
	public void testThreeServices() throws UnirestException {
		SerializedRequest[] requests = {
				RequestFactory.createEntityFremeNER("Brussels is the capital of Belgium", "en", "dbpedia"),
				RequestFactory.createTranslation("en", "fr")
		};
		String body = Serializer.toJson(requests);
		HttpResponse<String> response = post("chain")
				.queryString("stats", "true")
				.header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
				.body(new JsonNode(body))
				.asString();
		// print some response info
		logger.info("response.getStatus() = " + response.getStatus());
		logger.info("response.getStatusText() = " + response.getStatusText());
		logger.info("response.contentType = " + response.getHeaders().getFirst("content-type"));
		logger.debug("response.getBody() = " + response.getBody());
	}
}
