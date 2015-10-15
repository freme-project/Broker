/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.broker.integration_tests.pipelines;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.integration_tests.EServiceTest;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import eu.freme.eservices.pipelines.serialization.Pipeline;
import eu.freme.eservices.pipelines.serialization.Serializer;
import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * <p>Copyright 2015 MMLab, UGent</p>
 *
 * @author Gerald Haesendonck
 */
public abstract class PipelinesCommon extends EServiceTest {
	protected PipelinesCommon() {
		super("/pipelining/");
		enableAuthenticate();
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
	protected HttpResponse<String> sendRequest(int expectedResponseCode, final SerializedRequest... requests) throws UnirestException {
		List<SerializedRequest> serializedRequests = Arrays.asList(requests);
		String body = Serializer.toJson(requests);
		//System.out.println("request.body = " + body);

		HttpResponse<String> response = baseRequestPost("chain")
				.header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
				.body(new JsonNode(body))
				.asString();

		// print some response info
		logger.info("response.getStatus() = " + response.getStatus());
		logger.info("response.getStatusText() = " + response.getStatusText());
		logger.info("response.contentType = " + response.getHeaders().getFirst("content-type"));
		//System.out.println("response.getBody() = " + response.getBody());

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
	protected static RDFConstants.RDFSerialization getContentTypeOfLastResponse(final List<SerializedRequest> serializedRequests) {
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

	protected Pipeline createDefaultTemplate(final String token, final OwnedResource.Visibility visibility) throws UnirestException {
		SerializedRequest entityRequest = RequestFactory.createEntitySpotlight("en");
		SerializedRequest linkRequest = RequestFactory.createLink("3");    // Geo pos

		String serializedRequests = Serializer.toJson(entityRequest, linkRequest);
		Pipeline pipeline = new Pipeline("a label", "a description", serializedRequests);
		String body = Serializer.toJson(pipeline);
		HttpResponse<String> response = baseRequestPost("templates", token)
				.queryString("visibility", visibility.name())
				.header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
				.body(new JsonNode(body))
				.asString();
		// print some response info
		logger.info("response.getStatus() = " + response.getStatus());
		logger.info("response.getStatusText() = " + response.getStatusText());
		logger.info("response.contentType = " + response.getHeaders().getFirst("content-type"));
		logger.debug("response.body = " + response.getBody());
		assertEquals(HttpStatus.SC_OK, response.getStatus());
		Pipeline pipelineInfo = Serializer.templateFromJson(response.getBody());
		pipelineInfo.setSerializedRequests(serializedRequests);
		return pipelineInfo;
	}

	protected List<Pipeline> readTemplates(final String token) throws UnirestException {
		HttpResponse<String> response = baseRequestGet("templates", token).asString();
		assertEquals(HttpStatus.SC_OK, response.getStatus());
		return Serializer.templatesFromJson(response.getBody());
	}
}
