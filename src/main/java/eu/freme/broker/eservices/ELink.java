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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.exception.InvalidNIFException;
import eu.freme.broker.exception.InvalidTemplateEndpointException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.broker.tools.TemplateValidator;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.exception.OwnedResourceNotFoundException;
import eu.freme.common.exception.TemplateNotFoundException;
import eu.freme.common.exception.UnsupportedEndpointType;
import eu.freme.common.persistence.dao.TemplateDAO;
import eu.freme.common.persistence.dao.UserDAO;
import eu.freme.common.persistence.model.Template;
import eu.freme.common.persistence.model.User;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@Profile("broker")
public class ELink extends BaseRestController {

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
	@RequestMapping(value = "/e-link/documents", method = RequestMethod.POST)
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
	@RequestMapping(value = "/e-link/explore", method = RequestMethod.POST)
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

	// Creating a template.
	// POST /e-link/templates/
	// Example: curl -X POST -d @template.json
	// "http://localhost:8080/e-link/templates/" -H
	// "Content-Type: application/json" -H "Accept: application/json" -v
	@RequestMapping(value = "/e-link/templates", method = RequestMethod.POST)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public ResponseEntity<String> createTemplate(
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			// @RequestParam(value = "informat", required=false) String
			// informat,
			// @RequestParam(value = "f", required=false) String f,
			// @RequestParam(value = "outformat", required=false) String
			// outformat,
			// @RequestParam(value = "o", required=false) String o,
			// Type was moved as endpoint-type field of the template.
			// @RequestParam(value = "visibility", required=false) String
			// visibility,
			// Type was moved as endpoint-type field of the template.
			// @RequestParam(value = "type", required=false) String type,
			@RequestParam Map<String, String> allParams,
			@RequestBody String postBody) {

		try {

			NIFParameterSet nifParameters = this.normalizeNif(postBody,
					acceptHeader, contentTypeHeader, allParams, false);

			// NOTE: informat was defaulted to JSON before! Now it is TURTLE.
			// NOTE: outformat was defaulted to turtle, if acceptHeader=="*/*"
			// and informat==null, otherwise to JSON. Now it is TURTLE.
			// NOTE: switched back to JSON since we use MySQL and RDF is no
			// longer supported, but JSON only.
			nifParameters.setInformat(RDFSerialization.JSON);
			nifParameters.setOutformat(RDFSerialization.JSON);

			Template template;
			if (nifParameters.getInformat().equals(
					RDFConstants.RDFSerialization.JSON)) {
				JSONObject jsonObj = new JSONObject(nifParameters.getInput());
				templateValidator.validateTemplateEndpoint(jsonObj
						.getString("endpoint"));

				// AccessDeniedException can be thrown, if current
				// authentication is the anonymousUser
				template = new Template(jsonObj);
			} else {
				throw new BadRequestException(
						"Other formats then JSON are no longer supported for templates.");
				// Model model =
				// rdfConversionService.unserializeRDF(nifParameters.getInput(),
				// nifParameters.getInformat());
				// template = new Template(
				// OwnedResource.Visibility.getByString(visibility),
				// Template.Type.getByString(jsonObj.getString("endpoint-type")),
				// model);
				// templateValidator.validateTemplateEndpoint(template.getEndpoint());
			}

			template = templateDAO.save(template);

			String serialization;
			if (nifParameters.getOutformat().equals(
					RDFConstants.RDFSerialization.JSON)) {
				ObjectWriter ow = new ObjectMapper().writer()
						.withDefaultPrettyPrinter();
				serialization = ow.writeValueAsString(template);
			} else {
				// Should never fail to.
				serialization = rdfConversionService.serializeRDF(
						template.getRDF(), nifParameters.getOutformat());
			}

			HttpHeaders responseHeaders = new HttpHeaders();
			URI location = new URI("/e-link/templates/" + template.getId());
			responseHeaders.setLocation(location);
			responseHeaders.set("Content-Type", nifParameters.getOutformat()
					.contentType());
			// String serialization =
			// rdfConversionService.serializeRDF(template.getRDF(),
			// nifParameters.getOutformat());
			return new ResponseEntity<>(serialization, responseHeaders,
					HttpStatus.OK);
		} catch (AccessDeniedException ex) {
			logger.error(ex.getMessage(), ex);
			throw new eu.freme.broker.exception.AccessDeniedException(
					ex.getMessage());
		} catch (BadRequestException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		} catch (URISyntaxException | org.json.JSONException ex) {
			logger.error(ex.getMessage(), ex);
			throw new BadRequestException(ex.getMessage());
		} catch (InvalidTemplateEndpointException ex) {
			logger.error(ex.getMessage(), ex);
			throw new InvalidTemplateEndpointException(ex.getMessage());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new InternalServerErrorException(ex.getMessage());
		}
	}

	// Update one template.
	// PUT /e-link/templates/{template-id}
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.PUT)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public ResponseEntity<String> updateTemplateById(
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			// @RequestParam(value = "informat", required=false) String
			// informat,
			// @RequestParam(value = "f", required=false) String f,
			// @RequestParam(value = "outformat", required=false) String
			// outformat,
			// @RequestParam(value = "o", required=false) String o,
			@RequestParam(value = "owner", required = false) String ownerName,
			@PathVariable("templateid") long templateId,
			@RequestParam Map<String, String> allParams,
			@RequestBody String postBody) {

		try {
			// NOTE: informat was defaulted to JSON before! Now it is TURTLE.
			// NOTE: outformat was defaulted to turtle, if acceptHeader=="*/*"
			// and informat==null, otherwise to JSON. Now it is TURTLE.
			NIFParameterSet nifParameters = this.normalizeNif(postBody,
					acceptHeader, contentTypeHeader, allParams, true);

			// validateTemplateID(templateId);
			// check read access
			Template template = templateDAO.findOneById(templateId);

			// Was the nif-input empty?
			if (nifParameters.getInput() != null) {
				if (nifParameters.getInformat().equals(
						RDFConstants.RDFSerialization.JSON)) {
					JSONObject jsonObj = new JSONObject(
							nifParameters.getInput());
					template.update(jsonObj);
				} else {
					Model model = rdfConversionService.unserializeRDF(
							nifParameters.getInput(),
							nifParameters.getInformat());
					template.setTemplateWithModel(model);
				}
			}

			template = templateDAO.save(template);

			if (!Strings.isNullOrEmpty(ownerName)) {
				User owner = userDAO.getRepository().findOneByName(ownerName);
				if (owner == null)
					throw new BadRequestException(
							"Can not change owner of the template. User \""
									+ ownerName + "\" does not exist.");
				template = templateDAO.updateOwner(template, owner);
			}

			String serialization;
			if (nifParameters.getOutformat().equals(
					RDFConstants.RDFSerialization.JSON)) {
				ObjectWriter ow = new ObjectMapper().writer()
						.withDefaultPrettyPrinter();
				serialization = ow.writeValueAsString(template);
			} else {
				serialization = rdfConversionService.serializeRDF(
						template.getRDF(), nifParameters.getOutformat());
			}

			HttpHeaders responseHeaders = new HttpHeaders();
			URI location = new URI("/e-link/templates/" + template.getId());
			responseHeaders.setLocation(location);
			responseHeaders.set("Content-Type", nifParameters.getOutformat()
					.contentType());
			return new ResponseEntity<>(serialization, responseHeaders,
					HttpStatus.OK);
		} catch (URISyntaxException ex) {
			logger.error(ex.getMessage(), ex);
			throw new BadRequestException(ex.getMessage());
		} catch (AccessDeniedException ex) {
			logger.error(ex.getMessage(), ex);
			throw new eu.freme.broker.exception.AccessDeniedException();
		} catch (BadRequestException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		} catch (OwnedResourceNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
			throw new TemplateNotFoundException("Template not found. "
					+ ex.getMessage());
		} catch (org.json.JSONException ex) {
			logger.error(ex.getMessage(), ex);
			throw new BadRequestException(
					"The JSON object is incorrectly formatted. Problem description: "
							+ ex.getMessage());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		throw new InternalServerErrorException(
				"Unknown problem. Please contact us.");
	}

	// Get one template.
	// GET /e-link/templates/{template-id}
	// curl -v http://api-dev.freme-project.eu/current/e-link/templates/1
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.GET)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public ResponseEntity<String> getTemplateById(
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@PathVariable("templateid") long templateIdStr,
			// @RequestParam(value = "outformat", required=false) String
			// outformat,
			// @RequestParam(value = "o", required=false) String o
			@RequestParam Map<String, String> allParams) {

		try {
			// validateTemplateID(templateIdStr);
			// NOTE: outformat was defaulted to JSON before! Now it is TURTLE.
			NIFParameterSet nifParameters = this.normalizeNif(null,
					acceptHeader, null, allParams, true);
			HttpHeaders responseHeaders = new HttpHeaders();
			// check read access
			Template template = templateDAO.findOneById(templateIdStr);

			String serialization;
			if (nifParameters.getOutformat().equals(
					RDFConstants.RDFSerialization.JSON)) {
				ObjectWriter ow = new ObjectMapper().writer()
						.withDefaultPrettyPrinter();
				serialization = ow.writeValueAsString(template);
			} else {
				serialization = rdfConversionService.serializeRDF(
						template.getRDF(), nifParameters.getOutformat());
			}
			responseHeaders.set("Content-Type", nifParameters.getOutformat()
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
			logger.error(ex.getMessage(), ex);
			throw new TemplateNotFoundException("Template not found. "
					+ ex.getMessage());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new InternalServerErrorException(
					"Unknown problem. Please contact us.");
		}
	}

	// Retrieve all templates.
	// GET /e-link/templates/
	// curl -v http://api-dev.freme-project.eu/current/e-link/templates/
	@RequestMapping(value = "/e-link/templates", method = RequestMethod.GET)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public ResponseEntity<String> getAllTemplates(
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			// @RequestParam(value = "outformat", required=false) String
			// outformat,
			// @RequestParam(value = "o", required=false) String o,
			@RequestParam Map<String, String> allParams) {
		try {
			NIFParameterSet nifParameters = this.normalizeNif(null,
					acceptHeader, contentTypeHeader, allParams, true);

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set("Content-Type", nifParameters.getOutformat()
					.contentType());

			List<Template> templates = templateDAO.findAllReadAccessible();
			if (nifParameters.getOutformat().equals(
					RDFConstants.RDFSerialization.JSON)) {
				ObjectWriter ow = new ObjectMapper().writer()
						.withDefaultPrettyPrinter();
				String serialization = ow.writeValueAsString(templates);
				return new ResponseEntity<>(serialization, responseHeaders,
						HttpStatus.OK);
			} else {
				Model mergedModel = ModelFactory.createDefaultModel();
				for (Template template : templates) {
					mergedModel.add(template.getRDF());
				}
				return new ResponseEntity<>(rdfConversionService.serializeRDF(
						mergedModel, nifParameters.getOutformat()),
						responseHeaders, HttpStatus.OK);
			}
		} catch (BadRequestException ex) {
			throw new BadRequestException(ex.getMessage());
		} catch (Exception ex) {
			Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
		}
		throw new InternalServerErrorException(
				"Unknown problem. Please contact us.");
	}

	// Removing a template.
	// DELETE /e-link/templates/{template-id}
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.DELETE)
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public ResponseEntity<String> removeTemplateById(
			@PathVariable("templateid") long id) {
		try {
			// validateTemplateID(id);
			// check read and write access
			templateDAO.delete(templateDAO.findOneById(id));
			return new ResponseEntity<>(
					"The template was sucessfully removed.", HttpStatus.OK);
		} catch (AccessDeniedException ex) {
			logger.error(ex.getMessage(), ex);
			throw new eu.freme.broker.exception.AccessDeniedException();
		} catch (BadRequestException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		} catch (OwnedResourceNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
			throw new TemplateNotFoundException("Template not found. "
					+ ex.getMessage());
		}
	}

	/*
	 * private int validateTemplateID(String templateId) throws
	 * BadRequestException{ if(templateId.isEmpty()){ throw new
	 * BadRequestException("Empty templateid parameter."); } for(int i = 0; i <
	 * templateId.length(); i++) { if(i == 0 && templateId.charAt(i) == '-') {
	 * if(templateId.length() == 1) { throw new
	 * BadRequestException("The templateid parameter is not integer."); } else
	 * continue; } if(Character.digit(templateId.charAt(i),10) < 0) { throw new
	 * BadRequestException("The templateid parameter is not integer."); } }
	 * return Integer.parseInt(templateId); }
	 */
}
