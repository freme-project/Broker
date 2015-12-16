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
import eu.freme.broker.exception.*;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.exception.OwnedResourceNotFoundException;
import eu.freme.common.persistence.dao.PipelineDAO;
import eu.freme.common.persistence.dao.UserDAO;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.Pipeline;
import eu.freme.common.persistence.model.User;
import eu.freme.eservices.pipelines.core.PipelineResponse;
import eu.freme.eservices.pipelines.core.PipelineService;
import eu.freme.eservices.pipelines.core.ServiceException;
import eu.freme.eservices.pipelines.core.WrappedPipelineResponse;
import eu.freme.eservices.pipelines.requests.RequestBuilder;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import eu.freme.eservices.pipelines.serialization.Serializer;
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

	@Autowired
	UserDAO userDAO;

	/**
	 * <p>Calls the pipelining service.</p>
	 * <p>Some predefined Requests can be formed using the class {@link RequestFactory}. It also converts request objects
	 * from and to JSON.</p>
	 * <p><To create custom requests, use the {@link RequestBuilder}.</p>
	 * <p>Examples can be found in the unit tests in {@link eu/freme/broker/integration_tests/pipelines}.</p>
	 * @param requests	The requests to send to the service.
	 * @param stats		If "true": wrap the response of the last request and add timing statistics.
	 * @return          The response of the last request.
	 * @throws BadRequestException				The contents of the request is not valid.
	 * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
	 */
	@RequestMapping(value = "pipelining/chain",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = {"text/turtle", "application/json", "application/ld+json", "application/n-triples", "application/rdf+xml", "text/n3", "text/html"}
	)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> pipeline(@RequestBody String requests, @RequestParam (value = "stats", defaultValue = "false", required = false) String stats) {
		try {
			boolean wrapResult = Boolean.parseBoolean(stats);
			List<SerializedRequest> serializedRequests = Serializer.fromJson(requests);
			WrappedPipelineResponse pipelineResult = pipelineAPI.chain(serializedRequests);
			MultiValueMap<String, String> headers = new HttpHeaders();

			if (wrapResult) {
				headers.add(HttpHeaders.CONTENT_TYPE, RDFConstants.RDFSerialization.JSON.contentType());
				return new ResponseEntity<>(Serializer.toJson(pipelineResult), headers, HttpStatus.OK);
			} else {
				headers.add(HttpHeaders.CONTENT_TYPE, pipelineResult.getContent().getContentType());
				PipelineResponse lastResponse = pipelineResult.getContent();
				return new ResponseEntity<>(lastResponse.getBody(), headers, HttpStatus.OK);
			}

		} catch (ServiceException serviceError) {
			// TODO: see if this can be replaced by excsption(s) defined in the broker.
			logger.error(serviceError.getMessage(), serviceError);
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, serviceError.getResponse().getContentType());
			return new ResponseEntity<>(serviceError.getMessage(), headers, serviceError.getStatus());
		} catch (JsonSyntaxException jsonException) {
			logger.error(jsonException.getMessage(), jsonException);
			String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
			throw new BadRequestException("Error detected in the JSON body contents: " + errormsg);
		} catch (UnirestException unirestException) {
			logger.error(unirestException.getMessage(), unirestException);
			throw new BadRequestException(unirestException.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			// throw an Internal Server exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}

	/**
	 * Calls the pipelining service using an existing template.
	 * @param body	The contents to send to the pipeline. This can be a NIF or plain text document.
	 * @param id	The id of the pipeline template to use.
	 * @param stats		If "true": wrap the response of the last request and add timing statistics.
	 * @return		The response of the latest request defined in the template.
	 * @throws AccessDeniedException			The pipeline template is not visible by the current user.
	 * @throws BadRequestException				The contents of the request is not valid.
	 * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
	 * @throws TemplateNotFoundException		The pipeline template does not exist.
	 */
	@RequestMapping(value = "pipelining/chain/{id}",
			method = RequestMethod.POST,
			consumes = {"text/turtle", "application/json", "application/ld+json", "application/n-triples", "application/rdf+xml", "text/n3", "text/plain"},
			produces = {"text/turtle", "application/json", "application/ld+json", "application/n-triples", "application/rdf+xml", "text/n3"}
	)
	public ResponseEntity<String> pipeline(@RequestBody String body, @PathVariable long id, @RequestParam (value = "stats", defaultValue = "false", required = false) String stats) {
		try {
			Pipeline pipeline = pipelineDAO.findOneById(id);
			List<SerializedRequest> serializedRequests = Serializer.fromJson(pipeline.getSerializedRequests());
			serializedRequests.get(0).setBody(body);
			return pipeline(Serializer.toJson(serializedRequests), stats);
		} catch (org.springframework.security.access.AccessDeniedException | InsufficientAuthenticationException ex) {
			logger.error(ex.getMessage(), ex);
			throw new AccessDeniedException(ex.getMessage());
		} catch (JsonSyntaxException jsonException) {
			logger.error(jsonException.getMessage(), jsonException);
			String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
			throw new BadRequestException("Error detected in the JSON body contents: " + errormsg);
		} catch (OwnedResourceNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
			throw new TemplateNotFoundException("Could not find the pipeline template with id " + id);
		}
	}

	/**
	 * Creates and stores a pipeline template.
	 * @param pipelineInfo  A JSON string containing the fields "label", "description", "serializedRequests", which
	 *                      define the pipeline template.
	 * @param visibility	The visibility of the template. Can be {@literal PUBLIC} or {@literal PRIVATE}. PUBLIC means visible to anyone,
	 *                      PRIVATE means only visible to the currently authenticated user.
	 * @param persist		{@literal true}: store the template until deleted by someone, {@literal false} to guarantee
	 *                                       it to be stored for one week.
	 * @return				A JSON string containing the full pipeline info, i.e. the fields "id", "label", "description",
	 * 						"persist", "visibility", "owner", "serializedRequests".
	 * @throws AccessDeniedException			The pipeline template is not visible by the current user.
	 * @throws BadRequestException				The contents of the request is not valid.
	 * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
	 */
	@RequestMapping(value = "pipelining/templates",
			method = RequestMethod.POST,
			consumes = "application/json",
			produces = "application/json"
	)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> create(
			@RequestBody String pipelineInfo,
			@RequestParam(value = "visibility", required = false) String visibility,
			@RequestParam (value = "persist", defaultValue = "false", required = false) String persist
	) {

		try {
			// just to perform a first validation of the pipeline...
			eu.freme.eservices.pipelines.serialization.Pipeline pipelineInfoObj = Serializer.templateFromJson(pipelineInfo);
			//List<SerializedRequest> serializedRequests = RequestFactory.fromJson(requests);

			boolean toPersist = Boolean.parseBoolean(persist);
			Pipeline pipeline = new Pipeline(
					OwnedResource.Visibility.getByString(visibility),
					pipelineInfoObj.getLabel(),
					pipelineInfoObj.getDescription(),
					Serializer.toJson(pipelineInfoObj.getSerializedRequests()),
					toPersist);
			pipeline = pipelineDAO.save(pipeline);
			String response = Serializer.toJson(pipeline);
			return createOKJSONResponse(response);
		} catch (JsonSyntaxException jsonException) {
			logger.error(jsonException.getMessage(), jsonException);
			String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
			throw new BadRequestException("Error detected in the JSON body contents: " + errormsg);
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

	/**
	 * Updates an existing pipeline template.
	 * @param id			The id of the pipeline template to update.
	 * @param ownerName		The name of the new owner.
	 * @param visibility    The visibility of the template. Can be {@literal PUBLIC} or {@literal PRIVATE}. PUBLIC means visible to anyone,
	 *                      PRIVATE means only visible to the currently authenticated user.
	 * @param persist       {@literal true}: store the template until deleted by someone, {@literal false} to guarantee
	 *                                       it to be stored for one week.
	 * @param pipelineInfo  A JSON string containing updated pipeline template info. The fields "label", "description", "serializedRequests"
	 *                      define the pipeline template.
	 * @return              A JSON string containing the updated full pipeline info, i.e. the fields "id", "label", "description",
	 * 						"persist", "visibility", "owner", "serializedRequests".
	 * @throws ForbiddenException				The pipeline template is not visible by the current user.
	 * @throws BadRequestException				The contents of the request is not valid.
	 * @throws TemplateNotFoundException		The pipeline template does not exist.
	 * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
	 */
	@RequestMapping(
			value = "pipelining/templates/{id}",
			method = RequestMethod.PUT,
			consumes = "application/json",
			produces = "application/json"
	)
	public ResponseEntity<String> update(
			@PathVariable(value = "id") long id,
			@RequestParam(value = "owner", required=false) String ownerName,
			@RequestParam(value = "visibility", required = false) String visibility,
			@RequestParam(value = "persist", required = false) String persist,
			@RequestBody(required = false) String pipelineInfo
	) {
		try {
			Pipeline pipeline = pipelineDAO.findOneById(id);
			if (pipelineInfo != null && !pipelineInfo.isEmpty()) {
				eu.freme.eservices.pipelines.serialization.Pipeline pipelineInfoObj = Serializer.templateFromJson(pipelineInfo);
				String newLabel = pipelineInfoObj.getLabel();
				if (newLabel != null && !newLabel.equals(pipeline.getLabel())) {
					pipeline.setLabel(newLabel);
				}
				String newDescription = pipelineInfoObj.getDescription();
				if (newDescription != null && !newDescription.equals(pipeline.getDescription())) {
					pipeline.setDescription(newDescription);
				}
				List<SerializedRequest> oldRequests = Serializer.fromJson(pipeline.getSerializedRequests());
				List<SerializedRequest> newRequests = pipelineInfoObj.getSerializedRequests();
				if (newRequests != null && !newRequests.equals(oldRequests)) {
					pipeline.setSerializedRequests(Serializer.toJson(newRequests));
				}
			}
			if (visibility != null && !visibility.equals(pipeline.getVisibility().name())) {
				pipeline.setVisibility(OwnedResource.Visibility.getByString(visibility));
			}
			if (persist != null) {
				boolean toPersist = Boolean.parseBoolean(persist);
				if (toPersist != pipeline.isPersistent()) {
					pipeline.setPersist(toPersist);
				}
			}
			if (ownerName != null && !ownerName.equals(pipeline.getOwner().getName())) {
				User newOwner = userDAO.getRepository().findOneByName(ownerName);
				if (newOwner == null) {
					throw new BadRequestException("Can not change owner of the dataset. User \"" + ownerName + "\" does not exist.");
				}
				pipeline.setOwner(newOwner);
			}
			pipeline = pipelineDAO.save(pipeline);
			String response = Serializer.toJson(pipeline);
			return createOKJSONResponse(response);
		} catch (org.springframework.security.access.AccessDeniedException | InsufficientAuthenticationException ex) {
			logger.error(ex.getMessage(), ex);
			throw new ForbiddenException(ex.getMessage());
		} catch (OwnedResourceNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
			throw new TemplateNotFoundException("Could not find the pipeline template with id " + id);
		} catch (JsonSyntaxException jsonException) {
			logger.error(jsonException.getMessage(), jsonException);
			String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
			throw new BadRequestException("Error detected in the JSON body contents: " + errormsg);
		} catch (eu.freme.common.exception.BadRequestException e) {
			logger.error(e.getMessage(), e);
			throw new BadRequestException(e.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			// throw an Internal Server exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}

	/**
	 * Reads (gets) the pipeline template with the given id.
	 * @param id  	The id of the pipeline template to get.
	 * @return		The pipeline templatewith the given id as a JSON string.
	 * @throws AccessDeniedException			The pipeline template is not visible by the current user.
	 * @throws TemplateNotFoundException		The pipeline template does not exist.
	 * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
	 */
	@RequestMapping(
			value = "pipelining/templates/{id}",
			method = RequestMethod.GET,
			produces = "application/json"
	)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> read(@PathVariable(value = "id") long id) {
		try {
			Pipeline pipeline = pipelineDAO.findOneById(id);
			String serializedPipeline = Serializer.toJson(pipeline);
			return createOKJSONResponse(serializedPipeline);
		} catch (org.springframework.security.access.AccessDeniedException | InsufficientAuthenticationException ex) {
			logger.error(ex.getMessage(), ex);
			throw new AccessDeniedException(ex.getMessage());
		} catch (OwnedResourceNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
			throw new TemplateNotFoundException("Could not find the pipeline template with id " + id);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			// throw an Internal Server exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}

	/**
	 * Reads (gets) all visible pipelines.
	 * @return  all visible pipelines as a JSON string.
	 */
	@RequestMapping(
			value = "pipelining/templates",
			method = RequestMethod.GET,
			produces = "application/json"
	)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> read() {
		try {
			List<Pipeline> readablePipelines = pipelineDAO.findAllReadAccessible();
			String serializedPipelines = Serializer.templatesToJson(readablePipelines);
			return createOKJSONResponse(serializedPipelines);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			// throw an Internal Server exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}

	/**
	 * Deletes the pipeline template with the given id.
	 * @param id	The id of the template to delete.
	 * @return      The message "The pipeline was sucessfully removed."
	 * @throws ForbiddenException				The pipeline template cannot be deleted by the current user.
	 * @throws TemplateNotFoundException		The pipeline template does not exist.
	 * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
	 */
	@RequestMapping(
			value = "pipelining/templates/{id}",
			method = RequestMethod.DELETE
	)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> delete(@PathVariable("id") long id) {
		try {
			pipelineDAO.delete(pipelineDAO.findOneById(id));
			return new ResponseEntity<>("The pipeline was sucessfully removed.", HttpStatus.OK);
		} catch (org.springframework.security.access.AccessDeniedException | InsufficientAuthenticationException ex) {
			logger.error(ex.getMessage(), ex);
			throw new ForbiddenException(ex.getMessage());
		} catch (OwnedResourceNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
			throw new TemplateNotFoundException("Could not find the pipeline template with id " + id);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			// throw an Internal Server exception if anything goes really wrong...
			throw new InternalServerErrorException(t.getMessage());
		}
	}

	private ResponseEntity<String> createOKJSONResponse(final String contents) {
		MultiValueMap<String, String> headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, RDFConstants.RDFSerialization.JSON.contentType());
		return new ResponseEntity<>(contents, headers, HttpStatus.OK);
	}
}