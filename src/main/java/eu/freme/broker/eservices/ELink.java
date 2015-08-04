package eu.freme.broker.eservices;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import eu.freme.broker.exception.TemplateNotFoundException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.eservices.elink.DataEnricher;
import eu.freme.eservices.elink.Exporter;
import eu.freme.eservices.elink.Template;
import eu.freme.eservices.elink.TemplateDAO;

@RestController
public class ELink extends BaseRestController {
    
        @Autowired
        DataEnricher dataEnricher;

        @Autowired
        TemplateDAO templateDAO;
        
        // Enriching using a template.        
        // POST /e-link/enrich/
        // Example: curl -X POST -d @data.ttl "http://localhost:8080/e-link/enrich/documents/?outformat=turtle&templateid=3&limit-val=4" -H "Content-Type: text/turtle"
	@RequestMapping(value = "/e-link/documents", method = RequestMethod.POST)
	public ResponseEntity<String> enrich(
			@RequestParam(value = "templateid",    required=true)  int    templateId,
			@RequestHeader(value = "Accept",       required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
                        @RequestBody String postBody,
                        @RequestParam Map<String,String> allParams) {
            try {
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
                
                String serialization;
                switch(parameters.getOutformat()) {
                    case TURTLE:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.TURTLE);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);                
                    case JSON_LD:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.JSON_LD);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                    case RDF_XML:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.RDF_XML);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                    case N_TRIPLES:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.N_TRIPLES);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                    case N3:
                        serialization = rdfConversionService.serializeRDF(inModel, RDFSerialization.N3);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                }
            } catch (TemplateNotFoundException ex) {
                logger.warn("The template with the specified ID has not been found.", ex);
                throw new TemplateNotFoundException("The template with the specified ID has not been found.");
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
	public ResponseEntity<String> createTemplate(
			@RequestHeader(value = "Accept",       required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
                        @RequestParam(value = "informat",      required=false) String informat,
                        @RequestParam(value = "f",             required=false) String f,
                        @RequestParam(value = "outformat",     required=false) String outformat,
                        @RequestParam(value = "o",             required=false) String o,
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
                        t = new Template(templateDAO.generateTemplateId(), jsonObj.getString("endpoint"), jsonObj.getString("query"));
                        break;
                    case TURTLE:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "TTL");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        t.setId(templateDAO.generateTemplateId());
                        break;
                    case JSON_LD:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "JSON-LD");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        t.setId(templateDAO.generateTemplateId());
                        break;
                    case RDF_XML:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "RDF/XML");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        t.setId(templateDAO.generateTemplateId());
                        break;
                    case N_TRIPLES:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "N-Triples");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        t.setId(templateDAO.generateTemplateId());
                        break;
                    case N3:
                        model.read(new ByteArrayInputStream(postBody.getBytes()), null, "N3");
                        t = Exporter.getInstance().model2OneTemplate(model);
                        t.setId(templateDAO.generateTemplateId());
                        break;                        
                }
                
                templateDAO.addTemplate(t);
                
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
            } catch (Exception ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
	}
        
        // Get one template.
        // GET /e-link/templates/{template-id}
        // curl -v http://api-dev.freme-project.eu/current/e-link/templates/1
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.GET)
	public ResponseEntity<String> getTemplateById(
                @RequestHeader(value = "Accept",       required=false) String acceptHeader,
                @PathVariable("templateid") String id,
                @RequestParam(value = "outformat",     required=false) String outformat,
                @RequestParam(value = "o",             required=false) String o) {
            
            try {
                
                if( outformat == null ){
                    outformat = o;
                }

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

                Template t = templateDAO.getTemplateById(id);
                
                if(t == null) {
                    throw new TemplateNotFoundException("Template with id: \"" + id + "\" does not exist.");
                }
                
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
                        model = templateDAO.getTemplateInRDFById(id);
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
                
            } 
            catch (TemplateNotFoundException e){
                throw new TemplateNotFoundException("Template not found.");
            }
            catch (Exception ex) {
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
                
            } catch (Exception ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw new InternalServerErrorException("Unknown problem. Please contact us.");
//            return new ResponseEntity<String>("Unknown problem. Please contact us.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
       
        // Update one template.
        // PUT /e-link/templates/{template-id}
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.PUT)
	public ResponseEntity<String> updateTemplateById(
			@RequestHeader(value = "Accept",       required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
                        @RequestParam(value = "informat",      required=false) String informat,
                        @RequestParam(value = "f",             required=false) String f,
                        @RequestParam(value = "outformat",     required=false) String outformat,
                        @RequestParam(value = "o",             required=false) String o,
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
                        t = new Template(templateId, jsonObj.getString("endpoint"), jsonObj.getString("query"));
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
            } catch (Exception ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            throw new InternalServerErrorException("Unknown problem. Please contact us.");            
        }
                
        // Removing a template.
        // DELETE /e-link/templates/{template-id}
	@RequestMapping(value = "/e-link/templates/{templateid}", method = RequestMethod.DELETE)
	public ResponseEntity<String> removeTemplateById(@PathVariable("templateid") String id) {
            
            if(templateDAO.removeTemplateById(id)) {
                return new ResponseEntity<String>("The template was sucessfully removed.", HttpStatus.NO_CONTENT);
            } else {
                throw new TemplateNotFoundException("A template with such id was not found.");
            }
        }
}
