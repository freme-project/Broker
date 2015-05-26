package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.eservices.elink.DataEnricher;
import eu.freme.eservices.elink.exceptions.TemplateNotFoundException;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class ELink extends BaseRestController {

	@Autowired
	DataEnricher dataEnricher;

	@RequestMapping(value = "/e-link/", method = RequestMethod.POST)
	public ResponseEntity<String> enrich(
			@RequestParam(value = "templateid", required=true) int templateId,
                        @RequestParam(value = "informat", required=false) String informat,
                        @RequestParam(value = "f", required=false) String f,
                        @RequestParam(value = "outformat", required=false) String outformat,
                        @RequestParam(value = "o", required=false) String o,
			@RequestHeader(value = "Accept", required=false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
                        @RequestBody String postBody) {
            try {

                if( postBody == null || postBody.trim().length() == 0 ){
                    return new ResponseEntity<String>("", HttpStatus.BAD_REQUEST);
                }

                Model model = ModelFactory.createDefaultModel();
                String serOutFormat = "";
                
                if(informat != null ) {
                    model = readModel(model, informat, postBody);
                    if(model == null)
                        return new ResponseEntity<String>("Invalid informat parameter.", HttpStatus.BAD_REQUEST);
                } else if (f != null) {
                    model = readModel(model, f, postBody);
                    if(model == null)
                        return new ResponseEntity<String>("Invalid informat parameter.", HttpStatus.BAD_REQUEST);
                } else if (contentTypeHeader != null) {
                    model = readModel(model, contentTypeHeader, postBody);
                    if(model == null)
                        return new ResponseEntity<String>("Invalid Content-Type value.", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                } else {
                    return new ResponseEntity<String>("Input format not specified."
                            + "Use the parameters informat or f, or using the Content-Type header set the input format.", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }
                
                if(outformat != null) {
                    switch(outformat) {
                        case "turtle":
                            serOutFormat = "text/turtle";
                            break;
                        case "text/turtle":
                            serOutFormat = "text/turtle";
                            break;
                        case "json-ld":
                            serOutFormat = "application/ld+json";
                            break;
                        case "application/json+ld":
                            serOutFormat = "application/ld+json";
                            break;
                        default:
                            return new ResponseEntity<String>("Specified output format is not supported.", HttpStatus.NOT_ACCEPTABLE);
                    }
                } else if (o != null) {
                    
                    switch(o) {
                        case "turtle":
                            serOutFormat = "text/turtle";
                            break;
                        case "text/turtle":
                            serOutFormat = "text/turtle";
                            break;
                        case "json-ld":
                            serOutFormat = "application/ld+json";
                            break;
                        case "application/json+ld":
                            serOutFormat = "application/ld+json";
                            break;
                        default:
                            return new ResponseEntity<String>("Specified output format is not supported.", HttpStatus.NOT_ACCEPTABLE);
                    }
                    
                } else if (acceptHeader != null) {
                    switch(acceptHeader) {
                        case "text/turtle":
                            serOutFormat = "text/turtle";
                            break;
                        case "json-ld":
                            serOutFormat = "application/ld+json";
                            break;
                        default:
                            return new ResponseEntity<String>("Specified output format is not supported.", HttpStatus.NOT_ACCEPTABLE);
//                            throw new BadRequestException("Unsupported output format: " + acceptHeader);
                    }
                } else {
                    // the output format is not set with the parameters or with the accept header
                    // the default is turtle
                    serOutFormat = "text/turtle";
                }

                model = dataEnricher.enrichNIF(model, templateId);
                
                String serialization;
                
                switch(serOutFormat) {
                    case "text/turtle":
                        serialization = rdfConversionService.serializeRDF(model, RDFSerialization.TURTLE);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);                
                        
                    case "application/ld+json":
                        serialization = rdfConversionService.serializeRDF(model, RDFSerialization.JSON_LD);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                }
            } catch (TemplateNotFoundException ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.INFO, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ELink.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
	}

    private Model readModel(Model model, String format, String postBody) {
        if(format.equals("turtle") || format.equals("text/turtle")) {
            model.read(new ByteArrayInputStream(postBody.getBytes()), null, "TTL");
        } else if (format.equals("json-ld") || format.equals("application/json+ld")) {
            model.read(new ByteArrayInputStream(postBody.getBytes()), null, "JSON-LD");
        } else {
            return null;
        }
        return model;
    }
        
}
