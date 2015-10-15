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
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.serialization.Pipeline;
import eu.freme.eservices.pipelines.serialization.Serializer;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Gerald Haesendonck
 */
public class CRUDTest extends PipelinesCommon {

	@Test
	public void testCreateDefault() throws UnirestException {
		Pipeline pipelineInfo = createDefaultTemplate(tokenWithPermission, OwnedResource.Visibility.PUBLIC);
		assertFalse(pipelineInfo.isPersist());
		assertTrue(pipelineInfo.getId() > 0);
	}

	@Test
	public void testCreateAndRead() throws UnirestException {
		Pipeline pipelineInfo = createDefaultTemplate(tokenWithPermission, OwnedResource.Visibility.PRIVATE);
		long id = pipelineInfo.getId();

		// now query pipeline with id
		HttpResponse<String> readResponse = baseRequestGet("templates/" + id, tokenWithPermission).asString();
		assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
		Pipeline readPipeline = Serializer.templateFromJson(readResponse.getBody());
		assertEquals(pipelineInfo.getId(), readPipeline.getId());
		assertEquals(pipelineInfo.getSerializedRequests(), readPipeline.getSerializedRequests());
	}

	@Test
	public void testCreatePrivateWithOneAndReadWithOther() throws UnirestException {
		Pipeline pipelineInfo = createDefaultTemplate(tokenWithPermission, OwnedResource.Visibility.PRIVATE);
		long id = pipelineInfo.getId();

		// now query pipeline with id
		HttpResponse<String> readResponse = baseRequestGet("templates/" + id, tokenWithPermission).asString();
		assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
		Pipeline readPipeline = Serializer.templateFromJson(readResponse.getBody());
		assertEquals(pipelineInfo.getId(), readPipeline.getId());
		assertEquals(pipelineInfo.getSerializedRequests(), readPipeline.getSerializedRequests());

		// now try to read pipeline with other user
		logger.info("You will see some AccessDeniedExceptions - this is OK.");
		HttpResponse<String> readResponseOther = baseRequestGet("templates/" + id, tokenWithOutPermission).asString();
		assertEquals(HttpStatus.SC_UNAUTHORIZED, readResponseOther.getStatus());
		logger.info("Response for unauthorized user: " + readResponseOther.getBody());
	}

	@Test
	public void testCreateAndReadMultiple() throws UnirestException {
		logger.info("Creating one public and one private pipeline per user");
		createDefaultTemplate(tokenWithPermission, OwnedResource.Visibility.PUBLIC);
		createDefaultTemplate(tokenWithPermission, OwnedResource.Visibility.PRIVATE);
		createDefaultTemplate(tokenWithOutPermission, OwnedResource.Visibility.PUBLIC);
		createDefaultTemplate(tokenWithOutPermission, OwnedResource.Visibility.PRIVATE);

		// now try to read pipeline with other user
		logger.info("Each user tries to read pipelines; only 3 should be visible.");
		List<Pipeline> pipelinesFromUser1 = readTemplates(tokenWithPermission);
		assertTrue(pipelinesFromUser1.size() >= 3);	// TODO: delete pipelines after each test, then this can be "equals"
		for (Pipeline pipeline : pipelinesFromUser1) {
			assertTrue(pipeline.getOwner().equals(usernameWithPermission) || pipeline.getVisibility().equals(OwnedResource.Visibility.PUBLIC.name()));
		}
		List<Pipeline> pipelinesFromUser2 = readTemplates(tokenWithOutPermission);
		assertTrue(pipelinesFromUser2.size() >= 3);
		for (Pipeline pipeline : pipelinesFromUser2) {
			assertTrue(pipeline.getOwner().equals(usernameWithoutPermission) || pipeline.getVisibility().equals(OwnedResource.Visibility.PUBLIC.name()));
		}
		List<Pipeline> pipelinesFromAdmin = readTemplates(tokenAdmin);
		assertTrue(pipelinesFromAdmin.size() >= 2);
		// TODO: shouldn't the admin see all templates?
	}
}
