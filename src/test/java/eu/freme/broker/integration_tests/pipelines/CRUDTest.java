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
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import eu.freme.eservices.pipelines.serialization.Pipeline;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Gerald Haesendonck
 */
public class CRUDTest extends PipelinesCommon {

	@Test
	public void testCreateDefault() throws UnirestException {
		SerializedRequest entityRequest = RequestFactory.createEntitySpotlight("en");
		SerializedRequest linkRequest = RequestFactory.createLink("3");    // Geo pos

		List<SerializedRequest> serializedRequests = Arrays.asList(entityRequest, linkRequest);
		String body = RequestFactory.toJson(serializedRequests);
		HttpResponse<String> response = baseRequestPost("templates", tokenWithPermission)
				.header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
				.body(new JsonNode(body))
				.asString();
		// print some response info
		logger.info("response.getStatus() = " + response.getStatus());
		logger.info("response.getStatusText() = " + response.getStatusText());
		logger.info("response.contentType = " + response.getHeaders().getFirst("content-type"));
		logger.debug("response.body = " + response.getBody());
		assertEquals(HttpStatus.SC_OK, response.getStatus());
		Pipeline pipelineInfo = RequestFactory.templateFromJson(response.getBody());
		assertFalse(pipelineInfo.isPersist());
		assertTrue(pipelineInfo.getId() > 0);
	}

	@Test
	public void testCreateAndRead() throws UnirestException {
		SerializedRequest entityRequest = RequestFactory.createEntitySpotlight("en");
		SerializedRequest linkRequest = RequestFactory.createLink("3");    // Geo pos

		List<SerializedRequest> serializedRequests = Arrays.asList(entityRequest, linkRequest);
		String body = RequestFactory.toJson(serializedRequests);
		HttpResponse<String> response = baseRequestPost("templates", tokenWithPermission)
				.header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
				.body(new JsonNode(body))
				.asString();
		assertEquals(HttpStatus.SC_OK, response.getStatus());

		// get id of pipeline
		String pipelineInfoStr = response.getBody();
		Pipeline pipelineInfo = RequestFactory.templateFromJson(pipelineInfoStr);
		long id = pipelineInfo.getId();

		// now query pipeline with id
		HttpResponse<String> readResponse = baseRequestGet("templates/" + id, tokenWithPermission).asString();
		assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
		Pipeline readPipeline = RequestFactory.templateFromJson(readResponse.getBody());
		assertEquals(pipelineInfo.getId(), readPipeline.getId());
		//assertEquals(pipelineInfo.getOwner(), readPipeline.getOwner());

		assertEquals(serializedRequests, readPipeline.getSerializedRequests());
	}

	@Test
	public void testCreatePrivateWithOneAndReadWithOther() throws UnirestException {
		SerializedRequest entityRequest = RequestFactory.createEntitySpotlight("en");
		SerializedRequest linkRequest = RequestFactory.createLink("3");    // Geo pos

		List<SerializedRequest> serializedRequests = Arrays.asList(entityRequest, linkRequest);
		String body = RequestFactory.toJson(serializedRequests);
		HttpResponse<String> response = baseRequestPost("templates", tokenWithPermission)
				.queryString("visibility", OwnedResource.Visibility.PRIVATE.name())
				.header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
				.body(new JsonNode(body))
				.asString();
		assertEquals(HttpStatus.SC_OK, response.getStatus());

		// get id of pipeline
		String pipelineInfoStr = response.getBody();
		Pipeline pipelineInfo = RequestFactory.templateFromJson(pipelineInfoStr);
		long id = pipelineInfo.getId();

		// now query pipeline with id
		HttpResponse<String> readResponse = baseRequestGet("templates/" + id, tokenWithPermission).asString();
		assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
		Pipeline readPipeline = RequestFactory.templateFromJson(readResponse.getBody());
		assertEquals(pipelineInfo.getId(), readPipeline.getId());
		//assertEquals(pipelineInfo.getOwner(), readPipeline.getOwner());

		assertEquals(serializedRequests, readPipeline.getSerializedRequests());

		// now try to read pipeline with other user
		logger.info("You will see some AccessDeniedExceptions - this is OK.");
		HttpResponse<String> readResponseOther = baseRequestGet("templates/" + id, tokenWithOutPermission).asString();
		assertEquals(HttpStatus.SC_UNAUTHORIZED, readResponseOther.getStatus());
		logger.info("Response for unauthorized user: " + readResponseOther.getBody());
	}
}
