/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * 					Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * 					Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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

import com.hp.hpl.jena.rdf.model.Model;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.exception.InvalidNIFException;
import eu.freme.broker.exception.InvalidTemplateEndpointException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.broker.tools.TemplateValidator;
import eu.freme.common.exception.OwnedResourceNotFoundException;
import eu.freme.common.exception.TemplateNotFoundException;
import eu.freme.common.exception.UnsupportedEndpointType;
import eu.freme.common.persistence.dao.TemplateDAO;
import eu.freme.common.persistence.dao.UserDAO;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.Template;
import eu.freme.common.persistence.tools.AccessLevelHelper;
import eu.freme.eservices.elink.api.DataEnricher;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/e-link")
@Profile("broker")
public class ELink extends RestrictedResourceManagingController<Template> {

	@Autowired
	AbstractAccessDecisionManager decisionManager;

	@Autowired
	UserDAO userDAO;

	@Autowired
	TemplateDAO templateDAO;

	@Autowired
	DataEnricher dataEnricher;

	@Autowired
	AccessLevelHelper accessLevelHelper;

	@Autowired
	TemplateValidator templateValidator;

	// Enriching using a template.
	// POST /e-link/enrich/
	// Example: curl -X POST -d @data.ttl
	// "http://localhost:8080/e-link/enrich/documents/?outformat=turtle&templateid=3&limit-val=4"
	// -H "Content-Type: text/turtle"
	@RequestMapping(value = "/documents", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public ResponseEntity<String> enrich(
			@RequestParam(value = "templateid", required = true) String templateIdStr,
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			@RequestBody String postBody,
			@RequestParam Map<String, String> allParams) {
		try {

			Long templateId;
			try {
				templateId = new Long(templateIdStr);
			} catch (NumberFormatException e) {
				logger.error(e);
				String msg = "Parameter templateid is required to be a numeric value.";
				throw new BadRequestException(msg);
			}

			// int templateId = validateTemplateID(templateIdStr);
			NIFParameterSet nifParameters = this.normalizeNif(postBody,
					acceptHeader, contentTypeHeader, allParams, false);

			// templateDAO.findOneById(templateIdStr);
			// Check read access and retrieve the template
			Template template = templateDAO.findOneById(templateId);

			HashMap<String, String> templateParams = new HashMap<>();

			for (Map.Entry<String, String> entry : allParams.entrySet()) {
				if (!nifParameterFactory.isNIFParameter(entry.getKey())) {
					templateParams.put(entry.getKey(), entry.getValue());
				}
			}

			Model inModel = rdfConversionService.unserializeRDF(
					nifParameters.getInput(), nifParameters.getInformat());
			inModel = dataEnricher.enrichWithTemplate(inModel, template,
					templateParams);

			HttpHeaders responseHeaders = new HttpHeaders();
			String serialization = rdfConversionService.serializeRDF(inModel,
					nifParameters.getOutformat());
			responseHeaders.add("Content-Type", nifParameters.getOutformat()
					.contentType());
			return new ResponseEntity<>(serialization, responseHeaders,
					HttpStatus.OK);
		} catch (AccessDeniedException ex) {
			logger.error(ex.getMessage(), ex);
			throw new eu.freme.broker.exception.AccessDeniedException();
		} catch (BadRequestException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		} catch (OwnedResourceNotFoundException ex) {
			logger.error(ex.getMessage());
			throw new TemplateNotFoundException(ex.getMessage());
		} catch (org.apache.jena.riot.RiotException ex) {
			logger.error("Invalid NIF document. " + ex.getMessage(), ex);
			throw new InvalidNIFException(ex.getMessage());
		} catch (Exception ex) {
			logger.error(
					"Internal service problem. Please contact the service provider.",
					ex);
			throw new InternalServerErrorException(
					"Unknown problem. Please contact us.");
		}
	}

	// Enriching using a template.
	// POST /e-link/explore/
	// Example: curl -v -X POST
	// "http://localhost:8080/e-link/explore?resource=http%3A%2F%2Fdbpedia.org%2Fresource%2FBerlin&endpoint=http%3A%2F%2Fdbpedia.org%2Fsparql&outformat=n-triples"
	// -H "Content-Type:"
	@RequestMapping(value = "/explore", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public ResponseEntity<String> exploreResource(
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			@RequestParam Map<String, String> allParams,
			@RequestParam(value = "resource", required = true) String resource,
			@RequestParam(value = "endpoint", required = true) String endpoint,
			@RequestParam(value = "endpoint-type", required = false) String endpointType) {
		try {

                    templateValidator.validateTemplateEndpoint(endpoint);
			NIFParameterSet nifParameters = this.normalizeNif("", acceptHeader,
					contentTypeHeader, allParams, false);

			Model inModel = dataEnricher.exploreResource(resource, endpoint,
					endpointType);

			HttpHeaders responseHeaders = new HttpHeaders();
			String serialization = rdfConversionService.serializeRDF(inModel,
					nifParameters.getOutformat());
			responseHeaders.add("Content-Type", nifParameters.getOutformat()
					.contentType());
			return new ResponseEntity<>(serialization, responseHeaders,
					HttpStatus.OK);
		} catch (InvalidTemplateEndpointException ex) {
			logger.error(ex.getMessage(), ex);
			throw new InvalidTemplateEndpointException(ex.getMessage());
		} catch (UnsupportedEndpointType | BadRequestException
				| UnsupportedOperationException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		} catch (Exception ex) {
			logger.error(
					"Internal service problem. Please contact the service provider.",
					ex);
			throw new InternalServerErrorException(
					"Unknown problem. Please contact us.");
		}
	}

	@Override
	protected Template createEntity(String id, OwnedResource.Visibility visibility, String description, String body, Map<String, String> parameters) throws AccessDeniedException{
		JSONObject jsonObj = new JSONObject(body);
		templateValidator.validateTemplateEndpoint(jsonObj
				.getString("endpoint"));
		// AccessDeniedException can be thrown, if current authentication is the anonymousUser
		Template template = new Template(jsonObj);
		return template;
	}

	@Override
	protected void updateEntity(Template template, String body, Map<String, String> parameters) {
		JSONObject jsonObj = new JSONObject(body);
		template.update(jsonObj);
	}
}
