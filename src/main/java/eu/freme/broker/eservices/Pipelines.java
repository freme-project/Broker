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
package eu.freme.broker.eservices;

import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.exception.AccessDeniedException;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.exception.NotAcceptableException;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.dao.PipelineDAO;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.Pipeline;
import eu.freme.eservices.pipelines.core.PipelineResponse;
import eu.freme.eservices.pipelines.core.PipelineService;
import eu.freme.eservices.pipelines.core.ServiceException;
import eu.freme.eservices.pipelines.requests.RequestBuilder;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Gerald Haesendonck
 */
@RestController
@SuppressWarnings("unused")
@Profile("broker")
public class Pipelines extends BaseRestController {

	@Autowired
	PipelineService pipelineAPI;

	@Autowired
	PipelineDAO pipelineDAO;

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
			logger.error(serviceError.getMessage(), serviceError);
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, serviceError.getResponse().getContentType());
			return new ResponseEntity<>(serviceError.getMessage(), headers, serviceError.getStatus());
		} catch (JsonSyntaxException jsonException) {
			logger.error(jsonException.getMessage(), jsonException);
			String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
			throw new NotAcceptableException("Error detected in the JSON body contents: " + errormsg);
		} catch (UnirestException unirestException) {
			logger.error(unirestException.getMessage(), unirestException);
			throw new BadRequestException(unirestException.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			// throw an Internal Server exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}

	// TODO: comments
	@RequestMapping(value = "/pipelining/templates",
					method = RequestMethod.POST,
					consumes = "application/json",
					produces = "application/json"
	)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> create(
			@RequestBody String requests,
			@RequestParam(value = "visibility", required = false) String visibility,
			@RequestParam (value = "persist", defaultValue = "false", required = false) String persist
	) {

		try {
			// just to perform a first validation of the pipeline...
			List<SerializedRequest> serializedRequests = RequestFactory.fromJson(requests);

			boolean toPersist = Boolean.parseBoolean(persist);
			Pipeline pipeline = new Pipeline(OwnedResource.Visibility.getByString(visibility), "label", "description", requests, toPersist);
			pipelineDAO.save(pipeline);

			// now get the id of the pipeline.
			String response = "{\"id\": " + pipeline.getId() + ", \"persist\": " + pipeline.isPersistent() + '}';
			return createOKJSONResponse(response);
		} catch (JsonSyntaxException jsonException) {
			logger.error(jsonException.getMessage(), jsonException);
			String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
			throw new NotAcceptableException("Error detected in the JSON body contents: " + errormsg);
		} catch (eu.freme.common.exception.BadRequestException e) {
			logger.error(e.getMessage(), e);
			throw new BadRequestException(e.getMessage());
		} catch (org.springframework.security.access.AccessDeniedException | InsufficientAuthenticationException ex) {
			logger.error(ex.getMessage(), ex);
			throw new AccessDeniedException(ex.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			// throw an Internal Server exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}

	@RequestMapping(
			value = "pipelining/templates/{id}",
			method = RequestMethod.GET,
			produces = "application/json"
	)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> read(@PathVariable(value = "id") String id) {
		try {
			long idNr = Long.parseLong(id);
			Pipeline pipeline = pipelineDAO.findOneById(idNr);
			String serializedPipeline = RequestFactory.toJson(pipeline);
			return createOKJSONResponse(serializedPipeline);
		} catch (NumberFormatException ex) {
			logger.error(ex.getMessage(), ex);
			throw new BadRequestException("The id has to be an integer number. " + ex.getMessage());
		} catch (org.springframework.security.access.AccessDeniedException | InsufficientAuthenticationException ex) {
			logger.error(ex.getMessage(), ex);
			throw new AccessDeniedException(ex.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			// throw an Internal Server exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}

	@RequestMapping(
			value = "pipelining/templates",
			method = RequestMethod.GET,
			produces = "application/json"
	)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> read() {
		List<Pipeline> readablePipelines = pipelineDAO.findAllReadAccessible();
		String serializedPipelines = RequestFactory.templatesToJson(readablePipelines);
		return createOKJSONResponse(serializedPipelines);
	}

	private ResponseEntity<String> createOKJSONResponse(final String contents) {
		MultiValueMap<String, String> headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, RDFConstants.RDFSerialization.JSON.getMimeType());
		return new ResponseEntity<>(contents, headers, HttpStatus.OK);
	}
}