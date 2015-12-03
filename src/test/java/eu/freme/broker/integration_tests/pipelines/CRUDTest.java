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
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
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
		Pipeline pipelineInfo = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
		assertFalse(pipelineInfo.isPersist());
		assertTrue(pipelineInfo.getId() > 0);
		deleteTemplate(getTokenWithPermission(), pipelineInfo.getId(), HttpStatus.SC_OK);
	}

	@Test
	public void testCreateAndRead() throws UnirestException {
		Pipeline pipelineInfo = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PRIVATE);
		long id = pipelineInfo.getId();

		// now query pipeline with id
		HttpResponse<String> readResponse = addAuthentication(get("templates/" + id), getTokenWithPermission()).asString();
		assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
		Pipeline readPipeline = Serializer.templateFromJson(readResponse.getBody());
		assertEquals(pipelineInfo.getId(), readPipeline.getId());
		assertEquals(pipelineInfo.getSerializedRequests(), readPipeline.getSerializedRequests());
		deleteTemplate(getTokenWithPermission(), id, HttpStatus.SC_OK);
	}

	@Test
	public void testCreatePrivateWithOneAndReadWithOther() throws UnirestException {
		Pipeline pipelineInfo = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PRIVATE);
		long id = pipelineInfo.getId();

		// now query pipeline with id
		HttpResponse<String> readResponse = addAuthentication(get("templates/" + id), getTokenWithPermission()).asString();
		assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
		Pipeline readPipeline = Serializer.templateFromJson(readResponse.getBody());
		assertEquals(pipelineInfo.getId(), readPipeline.getId());
		assertEquals(pipelineInfo.getSerializedRequests(), readPipeline.getSerializedRequests());

		// now try to read pipeline with other user
		loggerIgnore(accessDeniedExceptions);
		HttpResponse<String> readResponseOther = addAuthentication(get("templates/" + id), getTokenWithOutPermission()).asString();
		assertEquals(HttpStatus.SC_UNAUTHORIZED, readResponseOther.getStatus());
		loggerUnignore(accessDeniedExceptions);
		logger.info("Response for unauthorized user: " + readResponseOther.getBody());

		deleteTemplate(getTokenWithPermission(), id, HttpStatus.SC_OK);
	}

	@Test
	public void testCreateAndReadMultiple() throws UnirestException {
		logger.info("Creating one public and one private pipeline per user");
		Pipeline pipeline1 = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
		Pipeline pipeline2 = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PRIVATE);
		Pipeline pipeline3 = createDefaultTemplate(getTokenWithOutPermission(), OwnedResource.Visibility.PUBLIC);
		Pipeline pipeline4 = createDefaultTemplate(getTokenWithOutPermission(), OwnedResource.Visibility.PRIVATE);

		// now try to read pipeline with other user
		logger.info("Each user tries to read pipelines; only 3 should be visible.");
		List<Pipeline> pipelinesFromUser1 = readTemplates(getTokenWithPermission());
		assertEquals(3, pipelinesFromUser1.size());	// TODO: delete pipelines after each test, then this can be "equals"
		for (Pipeline pipeline : pipelinesFromUser1) {
			assertTrue(pipeline.getOwner().equals(usernameWithPermission) || pipeline.getVisibility().equals(OwnedResource.Visibility.PUBLIC.name()));
		}
		List<Pipeline> pipelinesFromUser2 = readTemplates(getTokenWithOutPermission());
		assertEquals(3, pipelinesFromUser2.size());
		for (Pipeline pipeline : pipelinesFromUser2) {
			assertTrue(pipeline.getOwner().equals(usernameWithoutPermission) || pipeline.getVisibility().equals(OwnedResource.Visibility.PUBLIC.name()));
		}
		List<Pipeline> pipelinesFromAdmin = readTemplates(getTokenAdmin());
		assertEquals(2, pipelinesFromAdmin.size());
		// TODO: shouldn't the admin see all templates?

		deleteTemplate(getTokenWithPermission(), pipeline1.getId(), HttpStatus.SC_OK);
		deleteTemplate(getTokenWithPermission(), pipeline2.getId(), HttpStatus.SC_OK);
		deleteTemplate(getTokenWithOutPermission(), pipeline3.getId(), HttpStatus.SC_OK);
		deleteTemplate(getTokenWithOutPermission(), pipeline4.getId(), HttpStatus.SC_OK);
	}

	@Test
	public void testAllMethods() throws UnirestException {

		// create 2 templates
		Pipeline pipeline1 = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
		SerializedRequest nerRequest = RequestFactory.createEntityFremeNER("en", "dbpedia");
		SerializedRequest translateRequest = RequestFactory.createTranslation("en", "fr");
		Pipeline pipeline2 = createTemplate(getTokenWithPermission(), OwnedResource.Visibility.PRIVATE, "NER-Translate", "Apply FRENE NER and then e-Translate", nerRequest, translateRequest);

		// list the pipelines
		List<Pipeline> pipelines = readTemplates(getTokenWithPermission());
		assertEquals(pipeline1, pipelines.get(0));
		assertEquals(pipeline2, pipelines.get(1));

		// read individual pipelines
		Pipeline storedPipeline1 = readTemplate(getTokenWithPermission(), pipeline1.getId());
		Pipeline storedPipeline2 = readTemplate(getTokenWithPermission(), pipeline2.getId());
		assertEquals(pipeline1, storedPipeline1);
		assertEquals(pipeline2, storedPipeline2);

		// use pipelines
		String contents = "The Atomium in Brussels is the symbol of Belgium.";
		sendRequest(getTokenWithPermission(), HttpStatus.SC_OK, pipeline1.getId(), contents, RDFConstants.RDFSerialization.PLAINTEXT);
		sendRequest(getTokenWithPermission(), HttpStatus.SC_OK, pipeline2.getId(), contents, RDFConstants.RDFSerialization.PLAINTEXT);

		// update pipeline 1
		pipeline1.setVisibility(OwnedResource.Visibility.PRIVATE.name());
		updateTemplate(getTokenWithPermission(), pipeline1, HttpStatus.SC_OK);
		storedPipeline1 = readTemplate(getTokenWithPermission(), pipeline1.getId());
		assertEquals(pipeline1, storedPipeline1);

		// delete pipelines
		deleteTemplate(getTokenWithPermission(), pipeline1.getId(), HttpStatus.SC_OK);
		deleteTemplate(getTokenWithPermission(), pipeline2.getId(), HttpStatus.SC_OK);
	}

	@Test
	public void testDeleteNonExisting() throws UnirestException {
		loggerIgnore("eu.freme.common.exception.OwnedResourceNotFoundException || EXCEPTION ~=eu.freme.broker.exception.TemplateNotFoundException");
		deleteTemplate(getTokenWithPermission(), -5, HttpStatus.SC_NOT_FOUND);
		loggerUnignore("eu.freme.common.exception.OwnedResourceNotFoundException || EXCEPTION ~=eu.freme.broker.exception.TemplateNotFoundException");

	}

	@Test
	public void testDeleteFromAnother() throws UnirestException {
		Pipeline pipeline = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
		loggerIgnore("eu.freme.broker.exception.ForbiddenException");
		deleteTemplate(getTokenWithOutPermission(), pipeline.getId(), HttpStatus.SC_FORBIDDEN);
		loggerUnignore("eu.freme.broker.exception.ForbiddenException");
		deleteTemplate(getTokenWithPermission(), pipeline.getId(), HttpStatus.SC_OK);
	}

	@Test
	public void testSimpleUpdate() throws UnirestException {
		Pipeline pipeline = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
		pipeline.setDescription("This is a new description!");
		pipeline.setLabel("And a new label too!");
		String serialized = updateTemplate(getTokenWithPermission(), pipeline, HttpStatus.SC_OK);
		Pipeline newPipeline = Serializer.templateFromJson(serialized);
		assertEquals(pipeline, newPipeline);
		deleteTemplate(getTokenWithPermission(), pipeline.getId(), HttpStatus.SC_OK);
	}

	@Test
	public void testExecuteTemplate() throws UnirestException {
		Pipeline pipeline = createDefaultTemplate(getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
		long id = pipeline.getId();
		String contents = "The Atomium in Brussels is the symbol of Belgium.";
		HttpResponse<String> response = sendRequest(getTokenWithPermission(), HttpStatus.SC_OK, id, contents, RDFConstants.RDFSerialization.PLAINTEXT);
		deleteTemplate(getTokenWithPermission(), pipeline.getId(), HttpStatus.SC_OK);
	}
}
