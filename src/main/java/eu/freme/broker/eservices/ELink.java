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
import com.hp.hpl.jena.rdf.model.*;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.exception.InvalidTemplateEndpointException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.broker.tools.TemplateValidator;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.exception.OwnedResourceNotFoundException;
import eu.freme.common.exception.TemplateNotFoundException;
import eu.freme.common.exception.UnsupportedEndpointType;
import eu.freme.common.persistence.dao.TemplateDAO;
import eu.freme.common.persistence.dao.UserDAO;
import eu.freme.common.persistence.model.OwnedResource;
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
import org.springframework.security.core.context.SecurityContextHolder;
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

            int templateId;
            NIFParameterSet nifParameters;
            try {
                templateId = validateTemplateID(templateIdStr);
                nifParameters = this.normalizeNif(postBody, acceptHeader, contentTypeHeader, allParams, false);
            }catch(BadRequestException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

            try {
                // check read access
                if (templateDAO.findOneById(templateId + "") == null) {
                    throw new BadRequestException("template metadata for templateId=\""+templateId+"\" does not exist");
                }
            }catch (AccessDeniedException e){
                return new ResponseEntity<>("Access denied.", HttpStatus.FORBIDDEN);
            }catch (OwnedResourceNotFoundException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }

            HashMap<String, String> templateParams = new HashMap<>();

            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if(!nifParameterFactory.isNIFParameter(entry.getKey())){
                    templateParams.put(entry.getKey(), entry.getValue());
                }
            }

            Model inModel =  rdfConversionService.unserializeRDF(nifParameters.getInput(), nifParameters.getInformat());

            inModel = dataEnricher.enrichNIF(inModel, templateId, templateParams);

            HttpHeaders responseHeaders = new HttpHeaders();
            String serialization = rdfConversionService.serializeRDF(inModel, nifParameters.getOutformat());
            responseHeaders.add("Content-Type", nifParameters.getOutformat().getMimeType());
            return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
        } //catch (TemplateNotFoundException ex) {
        //  logger.warn("The template with the specified ID has not been found.", ex);
        //  throw new TemplateNotFoundException("The template with the specified ID has not been found.");
//            } catch (org.apache.jena.riot.RiotException ex) {
//                logger.error("Invalid NIF document.", ex);
//                throw new InvalidNIFException(ex.getMessage());                
        //}
        catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Internal service problem. Please contact the service provider.", ex);
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
        }
    }

    // Enriching using a template.
    // POST /e-link/explore/
    // Example: curl -v -X POST "http://localhost:8080/e-link/explore?resource=http%3A%2F%2Fdbpedia.org%2Fresource%2FBerlin&endpoint=http%3A%2F%2Fdbpedia.org%2Fsparql&outformat=n-triples" -H "Content-Type:"
    @RequestMapping(value = "/e-link/explore", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> exploreResource(
            @RequestHeader(value = "Accept",        required=false) String acceptHeader,
            @RequestHeader(value = "Content-Type",  required=false) String contentTypeHeader,
            @RequestParam Map<String,String>        allParams,
            @RequestParam(value = "resource",       required=true)  String resource,
            @RequestParam(value = "endpoint",      required=true)  String endpoint,
            @RequestParam(value = "endpoint-type", required=false) String endpointType) {
        try {

            System.out.println(resource);
            System.out.println(endpoint);
            System.out.println(endpointType);
//            int templateId;
            NIFParameterSet nifParameters;
            try {
                nifParameters = this.normalizeNif("", acceptHeader, contentTypeHeader, allParams, false);
            }catch(BadRequestException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
            
            Model inModel = dataEnricher.exploreResource(resource, endpoint, endpointType);

            HttpHeaders responseHeaders = new HttpHeaders();
            String serialization = rdfConversionService.serializeRDF(inModel, nifParameters.getOutformat());
            responseHeaders.add("Content-Type", nifParameters.getOutformat().getMimeType());
            return new ResponseEntity<String>(serialization, responseHeaders, HttpStatus.OK);
        } catch (UnsupportedEndpointType ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (UnsupportedOperationException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Internal service problem. Please contact the service provider.", ex);
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
        }
    }
    
    // Creating a template.
    // POST /e-link/templates/
    // Example: curl -X POST -d @template.json "http://localhost:8080/e-link/templates/" -H "Content-Type: application/json" -H "Accept: application/json" -v
    @RequestMapping(value = "/e-link/templates", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> createTemplate(
            @RequestHeader(value = "Accept",       required=false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
            //@RequestParam(value = "informat",      required=false) String informat,
            //@RequestParam(value = "f",             required=false) String f,
            //@RequestParam(value = "outformat",     required=false) String outformat,
            //@RequestParam(value = "o",             required=false) String o,
            @RequestParam(value = "visibility",        required=false) String visibility,
            @RequestParam Map<String,String> allParams,
            @RequestBody String postBody) {

        try {

            NIFParameterSet nifParameters;
            try {
                nifParameters = this.normalizeNif(postBody, acceptHeader, contentTypeHeader, allParams, false);
            }catch(BadRequestException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

            // NOTE: informat was defaulted to JSON before! Now it is TURTLE.
            // NOTE: outformat was defaulted to turtle, if acceptHeader=="*/*" and informat==null, otherwise to JSON. Now it is TURTLE.

            Template template;

            if(nifParameters.getInformat().equals(RDFConstants.RDFSerialization.JSON)){
                JSONObject jsonObj = new JSONObject(postBody);
                templateValidator.validateTemplateEndpoint(jsonObj.getString("endpoint"));

                //AccessDeniedException can be thrown, if current authentication is the anonymousUser
                try {
                    template = new Template(
                            OwnedResource.Visibility.getByString(visibility),
                            jsonObj.getString("endpoint"),
                            jsonObj.getString("query"),
                            jsonObj.getString("label"),
                            jsonObj.getString("description")
                    );
                }catch(AccessDeniedException e){
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
                }
            }else{
                Model model = rdfConversionService.unserializeRDF(nifParameters.getInput(), nifParameters.getInformat());
                template = new Template(OwnedResource.Visibility.getByString(visibility), model);
                templateValidator.validateTemplateEndpoint(template.getEndpoint());
            }

            try{
                templateDAO.save(template);
            } catch (AccessDeniedException e){
                // hardly possible, but checked ether
                return new ResponseEntity<String>("Access denied.", HttpStatus.FORBIDDEN);
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            URI location = new URI("/e-link/templates/"+template.getId());
            responseHeaders.setLocation(location);
            responseHeaders.set("Content-Type", nifParameters.getOutformat().getMimeType());
            String serialization = rdfConversionService.serializeRDF(template.getRDF(), nifParameters.getOutformat());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
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

    }

    // Get one template.
    // GET /e-link/templates/{template-id}
    // curl -v http://api-dev.freme-project.eu/current/e-link/templates/1
    @RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> getTemplateById(
            @RequestHeader(value = "Accept",       required=false) String acceptHeader,
            @PathVariable("templateid") String templateIdStr,
            //@RequestParam(value = "outformat",     required=false) String outformat,
            //@RequestParam(value = "o",             required=false) String o
            @RequestParam Map<String,String> allParams) {

        try {
            int templateId;
            NIFParameterSet nifParameters;
            try {
                templateId = validateTemplateID(templateIdStr);
                // NOTE: outformat was defaulted to JSON before! Now it is TURTLE.
                nifParameters = this.normalizeNif(null, acceptHeader, null, allParams, true);
            }catch(BadRequestException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

            HttpHeaders responseHeaders = new HttpHeaders();

            Template template;
            try {
                // check read access
                template = templateDAO.findOneById(templateId+"");
                if (template == null) {
                    throw new BadRequestException("template metadata for templateId=\""+templateId+"\" does not exist");
                }
            }catch (AccessDeniedException e){
                return new ResponseEntity<>("Access denied.", HttpStatus.FORBIDDEN);
            }catch (OwnedResourceNotFoundException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }

            String serialization;
            if(nifParameters.getOutformat().equals(RDFConstants.RDFSerialization.JSON)){
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                //JSONObject jsonObject = new JSONObject(ow.writeValueAsString(template));
                serialization = ow.writeValueAsString(template);//jsonObject.toString();//json;//gson.toJson(template); //Exporter.getInstance().convertOneTemplate2JSON(t).toString(4);
            }else {
                serialization = rdfConversionService.serializeRDF(template.getRDF(), nifParameters.getOutformat());
            }
            responseHeaders.set("Content-Type", nifParameters.getOutformat().getMimeType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);

        } catch (TemplateNotFoundException e) {
            throw new TemplateNotFoundException("Template not found.");
        } catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
        }

    }

    // Retrieve all templates.
    // GET /e-link/templates/
    // curl -v http://api-dev.freme-project.eu/current/e-link/templates/
    @RequestMapping(value = "/e-link/templates", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> getAllTemplates(
            @RequestHeader(value = "Accept",       required=false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
            //@RequestParam(value = "outformat",     required=false) String outformat,
            //@RequestParam(value = "o",             required=false) String o,
            @RequestParam Map<String, String> allParams) {
        try {
            NIFParameterSet nifParameters = this.normalizeNif(null, acceptHeader, contentTypeHeader,allParams,true);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Content-Type", nifParameters.getOutformat().getMimeType());

            List<Template> templates = templateDAO.findAllReadAccessible();
            if(nifParameters.getOutformat().equals(RDFConstants.RDFSerialization.JSON)){
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String serialization = ow.writeValueAsString(templates);
                return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
            }else {
                Model mergedModel = ModelFactory.createDefaultModel();
                for (Template template : templates) {
                    mergedModel.add(template.getRDF());
                }
                return new ResponseEntity<>(rdfConversionService.serializeRDF(mergedModel,nifParameters.getOutformat()), responseHeaders, HttpStatus.OK);
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
            //@RequestParam(value = "informat",      required=false) String informat,
            //@RequestParam(value = "f",             required=false) String f,
            //@RequestParam(value = "outformat",     required=false) String outformat,
            //@RequestParam(value = "o",             required=false) String o,
            @RequestParam(value = "visibility",    required=false) String visibility,
            @RequestParam(value = "owner",    required=false) String ownerName,
            @PathVariable("templateid") String templateId,
            @RequestParam Map<String,String> allParams,
            @RequestBody String postBody) {
        try {

            NIFParameterSet nifParameters;
            try {
                // NOTE: informat was defaulted to JSON before! Now it is TURTLE.
                // NOTE: outformat was defaulted to turtle, if acceptHeader=="*/*" and informat==null, otherwise to JSON. Now it is TURTLE.
                nifParameters = this.normalizeNif(postBody, acceptHeader, contentTypeHeader, allParams, true);
            }catch(BadRequestException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

            Template template;
            try {
                // check read access
                template = templateDAO.findOneById(templateId);
                if (template == null) {
                    throw new BadRequestException("template metadata for templateId=\""+templateId+"\" does not exist");
                }
                decisionManager.decide(SecurityContextHolder.getContext().getAuthentication(), template, accessLevelHelper.writeAccess());
            }catch (AccessDeniedException e){
                return new ResponseEntity<>("Access denied.", HttpStatus.FORBIDDEN);
            }catch (OwnedResourceNotFoundException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }

            // Was the nif-input empty?
            if(nifParameters.getInput()!=null) {
                if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.JSON)) {
                    JSONObject jsonObj = new JSONObject(nifParameters.getInput());
                    template.setEndpoint(jsonObj.getString("endpoint"));
                    template.setEndpoint(jsonObj.getString("endpoint"));
                    template.setLabel(jsonObj.getString("label"));
                    template.setDescription(jsonObj.getString("description"));
                } else {
                    Model model = rdfConversionService.unserializeRDF(nifParameters.getInput(), nifParameters.getInformat());
                    template.setTemplateWithModel(model);
                }
            }

            if(visibility!=null) {
                template.setVisibility(OwnedResource.Visibility.getByString(visibility));
            }
            if(ownerName!=null && !ownerName.trim().equals("")) {
                User owner = userDAO.getRepository().findOneByName(ownerName);
                if(owner==null)
                    throw new BadRequestException("Can not change owner of the dataset. User \""+ownerName+"\" does not exist.");
                template.setOwner(owner);
            }

            try {
                templateDAO.save(template);
            }catch (AccessDeniedException e){
                return new ResponseEntity<>("Access denied.", HttpStatus.FORBIDDEN);
            }

            String serialization = rdfConversionService.serializeRDF(template.getRDF(), nifParameters.getOutformat());
            HttpHeaders responseHeaders = new HttpHeaders();
            URI location = new URI("/e-link/templates/"+template.getId());
            responseHeaders.setLocation(location);
            responseHeaders.set("Content-Type", nifParameters.getOutformat().getMimeType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
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
            templateDAO.delete(templateDAO.findOneById(id));
            return new ResponseEntity<>("The template was sucessfully removed.", HttpStatus.OK);
        }catch (AccessDeniedException e){
            return new ResponseEntity<>("Access denied.", HttpStatus.FORBIDDEN);
        }catch (OwnedResourceNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }



    private int validateTemplateID(String templateId) throws BadRequestException{
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
