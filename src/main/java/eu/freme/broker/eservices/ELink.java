/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.freme.broker.security.database.model.Dataset;
import eu.freme.broker.security.database.model.OwnedResource;
import eu.freme.broker.security.database.dao.TemplateSecurityDAO;
import eu.freme.broker.security.database.dao.UserDAO;
import eu.freme.broker.security.database.model.User;
import eu.freme.broker.security.tools.AccessLevelHelper;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.exception.InvalidNIFException;
import eu.freme.broker.exception.InvalidTemplateEndpointException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.broker.tools.TemplateValidator;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.eservices.elink.api.DataEnricher;
import eu.freme.eservices.elink.Exporter;
import eu.freme.eservices.elink.Template;
import eu.freme.eservices.elink.TemplateDAO;
import eu.freme.eservices.elink.exceptions.TemplateNotFoundException;

@RestController
public class ELink extends BaseRestController {
    
        @Autowired
        DataEnricher dataEnricher;

        @Autowired
        TemplateDAO templateDAO;
        
        @Autowired
        AbstractAccessDecisionManager decisionManager;

        @Autowired
        UserDAO userDAO;
        //UserRepository userRepository;

        @Autowired
        TemplateSecurityDAO templateSecurityDAO;
        //TemplateRepository templateRepository;

        @Autowired
        AccessLevelHelper accessLevelHelper;

        // Enriching using a template.


        @Autowired
        TemplateValidator templateValidator;
        
        // Enriching using a template.        
        // POST /e-link/enrich/
        // Example: curl -X POST -d @data.ttl "http://localhost:8080/e-link/enrich/documents/?outformat=turtle&templateid=3&limit-val=4" -H "Content-Type: text/turtle"
	@RequestMapping(value = "/e-link/documents", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> enrich(
			@RequestParam(value = "templateid",    required=true)  String templateIdStr,
			@RequestHeader(value = "Accept",       required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
                        @RequestBody String postBody,
                        @RequestParam Map<String,String> allParams) {
            try {
                
                int templateId = validateTemplateID(templateIdStr);

                // Security
               try {
                   // check read access
                   if (templateSecurityDAO.findOneById(templateId + "") == null) {
                       throw new BadRequestException("template metadata for templateId=\""+templateId+"\" does not exist");
                   }
                }catch (AccessDeniedException e){
                   return new ResponseEntity<String>("Access denied.", HttpStatus.FORBIDDEN);
                }
                // Security END
                
                String informat  = null;
                String f         = null;
                String outformat = null;
                String o         = null;
                
                HashMap<String, String> templateParams = new HashMap();
                
                for (Map.Entry<String, String> entry : allParams.entrySet()) {
                    switch(entry.getKey()) {
                        case "informat":
                            informat = entry.getValue();
                            break;
                        case "f":
                            f = entry.getValue();
                            break;
                        case "outformat":
                            outformat = entry.getValue();
                            break;
                        case "o":
                            o = entry.getValue();
                            break;
                        default:
                            templateParams.put(entry.getKey(), entry.getValue());
                            break;
                    }
                }
                
                // merge long and short parameters - long parameters override short parameters.
                if( informat == null ){
                    informat = f;
                }
                if( outformat == null ){
                    outformat = o;
                }
                
                if( postBody == null || postBody.trim().length() == 0 ){
                    throw new eu.freme.broker.exception.BadRequestException("No data to process could be found in the input.");
                }
                
                NIFParameterSet parameters = this.normalizeNif(postBody, informat, outformat, postBody, acceptHeader, contentTypeHeader, null);

                Model inModel = ModelFactory.createDefaultModel();
                
                switch(parameters.getInformat()) {
                    case TURTLE:
                        inModel.read(new ByteArrayInputStream(postBody.getBytes()), null, "TTL");
                        break;
                    case JSON_LD:
                        inModel.read(new ByteArrayInputStream(postBody.getBytes()), null, "JSON-LD");
                        break;
                    case RDF_XML:
                        inModel.read(new ByteArrayInputStream(postBody.getBytes()), null, "RDF/XML");
                        break;
                    case N_TRIPLES:
                        inModel.read(new ByteArrayInputStream(postBody.getBytes()), null, "N-TRIPLE");
                        break;
                    case N3:
                        inModel.read(new ByteArrayInputStream(postBody.getBytes()), null, "N3");
                        break;                        
                }
                
                inModel = dataEnricher.enrichNIF(inModel, templateId, templateParams);
                
                HttpHeaders responseHeaders = new HttpHeaders();
                String serialization;
                switch(parameters.getOutformat()) {
                    case TURTLE:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.TURTLE);
                        responseHeaders.add("Content-Type", "text/turtle");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case JSON_LD:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.JSON_LD);
                        responseHeaders.add("Content-Type", "application/ld+json");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case RDF_XML:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.RDF_XML);
                        responseHeaders.add("Content-Type", "application/rdf+xml");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N_TRIPLES:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.N_TRIPLES);
                        responseHeaders.add("Content-Type", "application/n-triples");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N3:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.N3);
                        responseHeaders.add("Content-Type", "text/n3");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                }
            } catch (TemplateNotFoundException ex) {
                logger.warn("The template with the specified ID has not been found.", ex);
                throw new TemplateNotFoundException("The template with the specified ID has not been found.");
//            } catch (org.apache.jena.riot.RiotException ex) {
//                logger.error("Invalid NIF document.", ex);
//                throw new InvalidNIFException(ex.getMessage());                
            } catch (eu.freme.eservices.elink.exceptions.BadRequestException ex) {
                logger.error(ex.getMessage(), ex);
                throw ex;
            } catch (Exception ex) {
                logger.error("Internal service problem. Please contact the service provider.", ex);
                throw new InternalServerErrorException("Unknown problem. Please contact us.");
            }
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
	}
        
        // Creating a template.
        // POST /e-link/templates/
        // Example: curl -X POST -d @template.json "http://localhost:8080/e-link/templates/" -H "Content-Type: application/json" -H "Accept: application/json" -v
	@RequestMapping(value = "/e-link/templates", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> createTemplate(
			@RequestHeader(value = "Accept",       required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
            @RequestParam(value = "informat",      required=false) String informat,
            @RequestParam(value = "f",             required=false) String f,
            @RequestParam(value = "outformat",     required=false) String outformat,
            @RequestParam(value = "o",             required=false) String o,
            @RequestParam(value = "visibility",        required=false) String visibility,
            @RequestBody String postBody) {
            
            try {
                
                if( informat == null ){
                    informat = f;
                }
                if( outformat == null ){
                    outformat = o;
                }
            
                if( postBody == null || postBody.trim().length() == 0 ) {
                    return new ResponseEntity<String>("Empty body of the request.", HttpStatus.BAD_REQUEST);
                }
                
                // Checking the informat parameter
                RDFSerialization thisInformat;
                if (informat == null && contentTypeHeader == null) {
                    thisInformat = RDFSerialization.JSON;
		} else if (informat != null) {
                    if (!rdfELinkSerializationFormats.containsKey(informat)) {
                        throw new BadRequestException( "The parameter informat has invalid value \"" + informat + "\"");
                    }
                    thisInformat = rdfELinkSerializationFormats.get(informat);
		} else {
                    if (!rdfELinkSerializationFormats.containsKey(contentTypeHeader)) {
                        throw new BadRequestException("Content-Type header has invalid value \"" + contentTypeHeader + "\"");
                    }
                    thisInformat = rdfELinkSerializationFormats.get(contentTypeHeader);
		}
                // END: Checking the informat parameter
                
                // Checking the outformat parameter
                RDFSerialization thisOutformat;
		if( acceptHeader != null && acceptHeader.equals("*/*")) {
                    acceptHeader = "text/turtle";
		}
		if (outformat == null && acceptHeader == null) {
			thisOutformat = RDFSerialization.JSON;
		} else if (outformat != null) {
                    if (!rdfELinkSerializationFormats.containsKey(outformat)) {
                        throw new BadRequestException("Parameter outformat has invalid value \"" + outformat + "\"");
                    }
                    thisOutformat = rdfELinkSerializationFormats.get(outformat);
		} else {
                    if (!rdfELinkSerializationFormats.containsKey(acceptHeader)) {
			throw new BadRequestException("Parameter outformat has invalid value \"" + acceptHeader + "\"");
                    }
                    thisOutformat = rdfELinkSerializationFormats.get(acceptHeader);
		}
                // END: Checking the outformat parameter
                
                Template t = null;
                Model model = ModelFactory.createDefaultModel();
                
                switch(thisInformat) {
                    case JSON:
                        JSONObject jsonObj = new JSONObject(postBody);
                        templateValidator.validateTemplateEndpoint(jsonObj.getString("endpoint"));
                        t = new Template(
//                                templateDAO.generateTemplateId(),
                                jsonObj.getString("endpoint"),
                                jsonObj.getString("query"),
                                jsonObj.getString("label"),
                                jsonObj.getString("description")
                        );
                        break;
                    case TURTLE:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "TTL");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        templateValidator.validateTemplateEndpoint(t.getEndpoint());
                        t.setId(templateDAO.generateTemplateId());
                        break;
                    case JSON_LD:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "JSON-LD");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        templateValidator.validateTemplateEndpoint(t.getEndpoint());
                        t.setId(templateDAO.generateTemplateId());
                        break;
                    case RDF_XML:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "RDF/XML");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        templateValidator.validateTemplateEndpoint(t.getEndpoint());
                        t.setId(templateDAO.generateTemplateId());
                        break;
                    case N_TRIPLES:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "N-Triples");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        templateValidator.validateTemplateEndpoint(t.getEndpoint());
                        t.setId(templateDAO.generateTemplateId());
                        break;
                    case N3:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "N3");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        templateValidator.validateTemplateEndpoint(t.getEndpoint());
                        t.setId(templateDAO.generateTemplateId());
                        break;                        
                }
                
                templateDAO.addTemplate(t);
                
                String templateId = t.getId();

                // security
                try {
                    // check read access
                    if (templateSecurityDAO.findOneById(templateId) != null) {
                        throw new BadRequestException("template metadata for templateId=\""+templateId+"\" already exists");
                    }
                    templateSecurityDAO.save(new eu.freme.broker.security.database.model.Template(templateId, OwnedResource.Visibility.getByString(visibility)));
                } catch (AccessDeniedException e){
                    // TODO: not so cool...
                    templateDAO.removeTemplateById(templateId);
                    return new ResponseEntity<String>("Access denied.", HttpStatus.FORBIDDEN);
                }

                HttpHeaders responseHeaders = new HttpHeaders();
                URI location = new URI("/e-link/templates/"+t.getId());
                responseHeaders.setLocation(location);
                
                Model outModel;
                String serialization;
                
                switch(thisOutformat) {
                    case JSON:
                        responseHeaders.set("Content-Type", "application/json");
                        return new ResponseEntity<String>(Exporter.getInstance().convertOneTemplate2JSON(t).toString(), responseHeaders, HttpStatus.OK);
                    case TURTLE:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.TURTLE);
                        responseHeaders.set("Content-Type", "text/turtle");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);                
                    case JSON_LD:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.JSON_LD);
                        responseHeaders.set("Content-Type", "application/ld+json");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);                        
                    case RDF_XML:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.RDF_XML);
                        responseHeaders.set("Content-Type", "application/rdf+xml");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N_TRIPLES:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.N_TRIPLES);
                        responseHeaders.set("Content-Type", "application/n-triples");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N3:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.N3);
                        responseHeaders.set("Content-Type", "text/n3");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                }                
            } catch (URISyntaxException ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException(ex.getMessage());
            } catch (org.json.JSONException ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException(ex.getMessage());
            } catch (InvalidTemplateEndpointException ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
                throw new InvalidTemplateEndpointException(ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
                throw new InternalServerErrorException(ex.getMessage());
            }
            
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
	}
        
        // Get one template.
        // GET /e-link/templates/{template-id}
        // curl -v http://api-dev.freme-project.eu/current/e-link/templates/1
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> getTemplateById(
                @RequestHeader(value = "Accept",       required=false) String acceptHeader,
                @PathVariable("templateid") String idStr,
                @RequestParam(value = "outformat",     required=false) String outformat,
                @RequestParam(value = "o",             required=false) String o) {
            
            try {
                
                if( outformat == null ){
                    outformat = o;
                }
                
                int id = validateTemplateID(idStr);

                
                // Checking the outformat parameter
                RDFSerialization thisOutformat;
                if( acceptHeader != null && acceptHeader.equals("*/*")) {
                    acceptHeader = "application/json";
                }
                if (outformat == null && acceptHeader == null) {
                    thisOutformat = RDFSerialization.JSON;
                } else if (outformat != null) {
                    if (!rdfELinkSerializationFormats.containsKey(outformat)) {
                        throw new BadRequestException("Parameter outformat has invalid value \"" + outformat + "\"");
                    }
                    thisOutformat = rdfELinkSerializationFormats.get(outformat);
                } else {
                    if (!rdfELinkSerializationFormats.containsKey(acceptHeader)) {
                        throw new BadRequestException("Parameter outformat has invalid value \"" + acceptHeader + "\"");
                    }
                    thisOutformat = rdfELinkSerializationFormats.get(acceptHeader);
                }
                // END: Checking the outformat parameter

                // Security
                try {
                    // check read access
                    if (templateSecurityDAO.findOneById(id+"") == null) {
                        throw new BadRequestException("template metadata for templateId=\""+id+"\" does not exist");
                    }
                }catch (AccessDeniedException e){
                    return new ResponseEntity<String>("Access denied.", HttpStatus.FORBIDDEN);
                }
                // Security END

                Template t = templateDAO.getTemplateById(id+"");
                
                HttpHeaders responseHeaders = new HttpHeaders();
                Model model = ModelFactory.createDefaultModel();
                String serialization;
                
                switch(thisOutformat) {
                    case JSON:
                        responseHeaders.set("Content-Type", "application/json");
                        return new ResponseEntity<String>(Exporter.getInstance().convertOneTemplate2JSON(t).toString(4), responseHeaders, HttpStatus.OK);
                    case TURTLE:
                        model = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.TURTLE);
                        responseHeaders.set("Content-Type", "text/turtle");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case JSON_LD:
                        model = templateDAO.getTemplateInRDFById(id+"");
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.JSON_LD);
                        responseHeaders.set("Content-Type", "application/ld+json");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case RDF_XML:
                        model = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.RDF_XML);
                        responseHeaders.set("Content-Type", "application/rdf+xml");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N_TRIPLES:
                        model = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.N_TRIPLES);
                        responseHeaders.set("Content-Type", "application/n-triples");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N3:
                        model = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.N3);
                        responseHeaders.set("Content-Type", "text/n3");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                }
                
            } catch (TemplateNotFoundException e){
                throw new TemplateNotFoundException("Template not found.");
            } catch (BadRequestException ex) {
                logger.error(ex.getMessage(), ex);
                throw ex;
            } catch (Exception ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
        }

        // Retrieve all templates.
        // GET /e-link/templates/
        // curl -v http://api-dev.freme-project.eu/current/e-link/templates/
	@RequestMapping(value = "/e-link/templates", method = RequestMethod.GET)
	public ResponseEntity<String> getAllTemplates(
			@RequestHeader(value = "Accept",       required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
                        @RequestParam(value = "outformat",     required=false) String outformat,
                        @RequestParam(value = "o",             required=false) String o) {
            try {
                if( outformat == null ) {
                    outformat = o;
                }
                
                // Checking the outformat parameter
                RDFSerialization thisOutformat = null;
                thisOutformat = checkOutFormat(outformat, acceptHeader);
                // END: Checking the outformat parameter
                
                
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.APPLICATION_JSON);
                String serialization;
                Model model = ModelFactory.createDefaultModel();
                
                switch(thisOutformat) {
                    case JSON:
                        responseHeaders.set("Content-Type", "application/json");
                        return new ResponseEntity<String>(Exporter.getInstance().convertTemplates2JSON(templateDAO.getAllTemplates()).toString(4), responseHeaders, HttpStatus.OK);
                    case TURTLE:
                        model = templateDAO.getAllTemplatesInRDF();
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.TURTLE);
                        responseHeaders.set("Content-Type", "text/turtle");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case JSON_LD:
                        model = templateDAO.getAllTemplatesInRDF();
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.JSON_LD);
                        responseHeaders.set("Content-Type", "application/ld+json");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case RDF_XML:
                        model = templateDAO.getAllTemplatesInRDF();
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.RDF_XML);
                        responseHeaders.set("Content-Type", "application/rdf+xml");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N_TRIPLES:
                        model = templateDAO.getAllTemplatesInRDF();
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.N_TRIPLES);
                        responseHeaders.set("Content-Type", "application/n-triples");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N3:
                        model = templateDAO.getAllTemplatesInRDF();
                        serialization = rdfConversionService.serializeRDF(model, RDFConstants.RDFSerialization.N3);
                        responseHeaders.set("Content-Type", "text/n3");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                }
            } catch (BadRequestException ex) {
                throw new BadRequestException(ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
//            return new ResponseEntity<String>("Unknown problem. Please contact us.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
       
        // Update one template.
        // PUT /e-link/templates/{template-id}
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.PUT)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> updateTemplateById(
			@RequestHeader(value = "Accept",       required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
            @RequestParam(value = "informat",      required=false) String informat,
            @RequestParam(value = "f",             required=false) String f,
            @RequestParam(value = "outformat",     required=false) String outformat,
            @RequestParam(value = "o",             required=false) String o,
            @RequestParam(value = "visibility",    required=false) String visibility,
            @PathVariable("templateid") String templateId,
            @RequestBody String postBody) {
            try {
                
                if( informat == null ){
                    informat = f;
                }
                if( outformat == null ){
                    outformat = o;
                }
            
                if( postBody == null || postBody.trim().length() == 0 ) {
                    return new ResponseEntity<String>("Empty body of the request.", HttpStatus.BAD_REQUEST);
                }
                
                // Checking the informat parameter
                RDFSerialization thisInformat;
                if (informat == null && contentTypeHeader == null) {
                    thisInformat = RDFSerialization.JSON;
		} else if (informat != null) {
                    if (!rdfELinkSerializationFormats.containsKey(informat)) {
                        throw new BadRequestException( "The parameter informat has invalid value \"" + informat + "\"");
                    }
                    thisInformat = rdfELinkSerializationFormats.get(informat);
		} else {
                    if (!rdfELinkSerializationFormats.containsKey(contentTypeHeader)) {
                        throw new BadRequestException("Content-Type header has invalid value \"" + contentTypeHeader + "\"");
                    }
                    thisInformat = rdfELinkSerializationFormats.get(contentTypeHeader);
		}
                // END: Checking the informat parameter
                
                // Checking the outformat parameter
                RDFSerialization thisOutformat;
		if( acceptHeader != null && acceptHeader.equals("*/*")) {
                    acceptHeader = "text/turtle";
		}
		if (outformat == null && acceptHeader == null) {
			thisOutformat = RDFSerialization.JSON;
		} else if (outformat != null) {
                    if (!rdfELinkSerializationFormats.containsKey(outformat)) {
                        throw new BadRequestException("Parameter outformat has invalid value \"" + outformat + "\"");
                    }
                    thisOutformat = rdfELinkSerializationFormats.get(outformat);
		} else {
                    if (!rdfELinkSerializationFormats.containsKey(acceptHeader)) {
			throw new BadRequestException("Parameter outformat has invalid value \"" + acceptHeader + "\"");
                    }
                    thisOutformat = rdfELinkSerializationFormats.get(acceptHeader);
		}
                // Checking the outformat parameter

                Template t = null;
                Model model = ModelFactory.createDefaultModel();
                
                switch(thisInformat) {
                    case JSON:
                        JSONObject jsonObj = new JSONObject(postBody);
                        t = new Template(
                                templateId,
                                jsonObj.getString("endpoint"),
                                jsonObj.getString("query"),
                                jsonObj.getString("label"),
                                jsonObj.getString("description")
                        );
                        break;
                    case TURTLE:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "TTL");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        break;
                    case JSON_LD:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "JSON-LD");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        break;
                    case RDF_XML:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "RDF/XML");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        break;
                    case N_TRIPLES:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "N-Triples");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        break;
                    case N3:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "N3");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        break;
                }
               

                // Security
                try {
                    // check read access
                    eu.freme.broker.security.database.model.Template templ = templateSecurityDAO.findOneById(templateId);
                    if (templ == null) {
                        throw new BadRequestException("template metadata for templateId=\""+templateId+"\" does not exist");
                    }
                    if(visibility!=null) {
                        templ.setVisibility(OwnedResource.Visibility.getByString(visibility));
                    }
                    templateSecurityDAO.save(templ);
                }catch (AccessDeniedException e){
                    return new ResponseEntity<String>("Access denied.", HttpStatus.FORBIDDEN);
                }
                // Security END

                templateDAO.updateTemplate(t);
                
                HttpHeaders responseHeaders = new HttpHeaders();
                URI location = new URI("/e-link/templates/"+t.getId());
                responseHeaders.setLocation(location);
               
                Model outModel;
                String serialization;
                
                switch(thisOutformat) {
                    case JSON:
                        responseHeaders.set("Content-Type", "application/json");
                        return new ResponseEntity<String>(Exporter.getInstance().convertOneTemplate2JSON(t).toString(), responseHeaders, HttpStatus.OK);
                    case TURTLE:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.TURTLE);
                        responseHeaders.set("Content-Type", "text/turtle");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);                
                    case JSON_LD:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.JSON_LD);
                        responseHeaders.set("Content-Type", "application/ld+json");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);                        
                    case RDF_XML:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.RDF_XML);
                        responseHeaders.set("Content-Type", "application/rdf+xml");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N_TRIPLES:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.N_TRIPLES);
                        responseHeaders.set("Content-Type", "application/n-triples");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                    case N3:
                        outModel = templateDAO.getTemplateInRDFById(t.getId());
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.N3);
                        responseHeaders.set("Content-Type", "text/n3");
                        return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
                }
            } catch (URISyntaxException ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            } catch (org.json.JSONException ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("The JSON object is incorrectly formatted. Problem description: " + ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            throw new InternalServerErrorException("Unknown problem. Please contact us.");            
        }
                
        // Removing a template.
        // DELETE /e-link/templates/{template-id}
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.DELETE)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> removeTemplateById(@PathVariable("templateid") String id) {

            try {
                // check read and write access
                templateSecurityDAO.delete(templateSecurityDAO.getRepository().findOneById(id));
            }catch (AccessDeniedException e){
                return new ResponseEntity<String>("Access denied.", HttpStatus.FORBIDDEN);
            }

            if(templateDAO.removeTemplateById(id)) {
                return new ResponseEntity<String>("The template was sucessfully removed.", HttpStatus.NO_CONTENT);
            } else {
                throw new TemplateNotFoundException("A template with such id was not found.");
            }
        }

    private RDFSerialization checkOutFormat(String outformat, String acceptHeader) {
        if( acceptHeader != null && acceptHeader.equals("*/*")) {
            acceptHeader = "text/turtle";
        }
        if (outformat == null && acceptHeader == null) {
            return RDFSerialization.JSON;
        } else if (outformat != null) {
            if (!rdfELinkSerializationFormats.containsKey(outformat)) {
                throw new BadRequestException("Parameter outformat has invalid value \"" + outformat + "\"");
            }
            return rdfELinkSerializationFormats.get(outformat);
        } else {
            if (!rdfELinkSerializationFormats.containsKey(acceptHeader)) {
                throw new BadRequestException("Parameter outformat has invalid value \"" + acceptHeader + "\"");
            }
            return rdfELinkSerializationFormats.get(acceptHeader);
        }
    }

    // Updating dataset metadata
    @RequestMapping(value = "/e-link/templates/admin/{templateid}", method = {
            RequestMethod.PUT })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateTemplateMetadata(
            @PathVariable(value = "templateid") String name,
            @RequestParam(value = "owner",        required=false) String ownerName,
            @RequestParam(value = "visibility",        required=false) String visibility) {

        OwnedResource.Visibility newVisibility = OwnedResource.Visibility.PUBLIC;
        if(visibility!=null && !visibility.equals("")){
            newVisibility = OwnedResource.Visibility.getByString(visibility);
        }

        eu.freme.broker.security.database.model.Template template = templateSecurityDAO.findOneById(name);
        User owner;
        if(ownerName !=null) {
            owner = userDAO.getRepository().findOneByName(ownerName);
            if (owner == null) {
                return new ResponseEntity<String>("User \"" + ownerName + "\" does not exist.", HttpStatus.BAD_REQUEST);
            }
        }else{
            if(template!=null){
                owner = template.getOwner();

            }else{
                return new ResponseEntity<String>("Metadate for the template \"" + name + "\" does not exist. Please provide an owner to create this data.", HttpStatus.BAD_REQUEST);
            }
        }
        if(template==null){
            template = new eu.freme.broker.security.database.model.Template(name, owner, newVisibility);
        }else{
            template.setOwner(owner);
            // set visibility, if changed
            if(visibility!=null && !visibility.equals("")){
                template.setVisibility(newVisibility);
            }
        }


        // insert without permission check (via getRepository)
        templateSecurityDAO.save(template);

        return new ResponseEntity<String>("Update successful.", HttpStatus.OK);
    }

    private int validateTemplateID(String templateId) {
        if(templateId.isEmpty()){
            throw new BadRequestException("Empty templateid parameter.");
        }
        for(int i = 0; i < templateId.length(); i++) {
            if(i == 0 && templateId.charAt(i) == '-') {
                if(templateId.length() == 1) {
                    throw new BadRequestException("The templateid parameter is not integer.");
                }
                else continue;
            }
            if(Character.digit(templateId.charAt(i),10) < 0) {
                    throw new BadRequestException("The templateid parameter is not integer.");
            }
        }
        return Integer.parseInt(templateId);
    }
}
