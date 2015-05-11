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

import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.eservices.elink.DataEnricher;
import eu.freme.eservices.elink.exceptions.TemplateNotFoundException;
import java.io.ByteArrayInputStream;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class ELink extends BaseRestController {

	@Autowired
	DataEnricher dataEnricher;

	@RequestMapping(value = "/e-link/", method = RequestMethod.POST)
	public ResponseEntity<String> enrich(
			@RequestParam(value = "templateid", defaultValue = "") int templateId,
			@RequestHeader(value = "Accept") String acceptHeader,
			@RequestHeader(value = "Content-Type") String contentTypeHeader,
                        @RequestParam(value = "informat", defaultValue = "") String informat,
                        @RequestParam(value = "outformat", defaultValue = "") String outformat,
                        @RequestBody String body) {
            try {
                if( body == null || body.trim().length() == 0 ){
                    return new ResponseEntity<String>("", HttpStatus.BAD_REQUEST);
                }

                Model model = ModelFactory.createDefaultModel();
                String serOutFormat = "";

                switch(contentTypeHeader) {
                    case "text/turtle":
                        model.read(new ByteArrayInputStream(body.getBytes()), null, "TTL");
                        break;
                    case "application/rdf+xml":
                        model.read(new ByteArrayInputStream(body.getBytes()), null, "RDF/XML");
                        break;
                    case "application/ld+json":
                        model.read(new ByteArrayInputStream(body.getBytes()), null, "JSON-LD");
                        break;
                }
                
                if(informat != null || informat.trim().length() != 0) {
                    switch(informat) {
                        case "turtle":
                            model.read(new ByteArrayInputStream(body.getBytes()), null, "TTL");
                            break;
                        case "rdfxml":
                            model.read(new ByteArrayInputStream(body.getBytes()), null, "RDF/XML");
                            break;
                        case "json-ld":
                            model.read(new ByteArrayInputStream(body.getBytes()), null, "JSON-LD");
                            break;
                    }
                }
                
                serOutFormat = acceptHeader;
                
                if(outformat != null || outformat.trim().length() != 0) {
                    switch(outformat) {
                        case "turtle":
                            serOutFormat = "text/turtle";
                            break;
                        case "rdfxml":
                            serOutFormat = "application/rdf+xml";
                            break;
                        case "json-ld":
                            serOutFormat = "application/ld+json";
                            break;
                    }
                }

                model = dataEnricher.enrichNIF(model, templateId);

		// get output format
		RDFConstants.RDFSerialization outputFormat = getOutputSerialization(serOutFormat);
		String serialization;
		try {
                    serialization = rdfConversionService.serializeRDF(model, outputFormat);
		} catch (Exception ex) {
                    return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>(serialization, HttpStatus.OK);
                
            } catch (TemplateNotFoundException ex) {
                System.out.println("template not found");
    //            Logger.getLogger(ELinkAPI.class.getName()).log(Level.INFO, null, ex);
            }
            return null;
	}
}
