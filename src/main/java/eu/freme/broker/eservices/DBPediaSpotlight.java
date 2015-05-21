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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.freme.broker.exception.ExternalServiceFailedException;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;
import eu.freme.eservices.eentity.api.EEntityService;
import eu.freme.eservices.eentity.exceptions.BadRequestException;

import java.io.ByteArrayInputStream;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class DBPediaSpotlight extends BaseRestController {

	@Autowired
	EEntityService entityAPI;

	@Autowired
	RDFConversionService rdfConversionService;

	@RequestMapping(value = "/e-entity/dbpedia-spotlight", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> execute(
			@RequestParam(value = "input", defaultValue = "") String inputParam,
			@RequestParam(value = "language", defaultValue = "") String languageParam,
			@RequestParam(value = "confidence", defaultValue = "") String confidenceParam,
			@RequestHeader(value = "Accept", defaultValue = "") String acceptHeader,
			@RequestHeader(value = "Content-Type", defaultValue = "") String contentTypeHeader,
                        @RequestBody(required = false) String body) {
            Model inModel;
            String outModel;
                
            String textForProcessing = null;
            
            if(!inputParam.equals("")) {
                textForProcessing = inputParam;
                
            } else if (body != null) {
            
                inModel = ModelFactory.createDefaultModel();
                
                switch(contentTypeHeader) {

                    case "text/turtle":
                        inModel.read(new ByteArrayInputStream(body.getBytes()), null, "TTL");
                        break;
                        
                    case "application/x-turtle":
                        inModel.read(new ByteArrayInputStream(body.getBytes()), null, "TTL");
                        break;
                        
                    case "application/ld+json":
                        inModel.read(new ByteArrayInputStream(body.getBytes()), "JSON-LD");
                        break;
                        
                    case "application/rdf+xml":
                        inModel.read(new ByteArrayInputStream(body.getBytes()), "RDF/XML");
                        break;
                }
                    
                
                StmtIterator iter = inModel.listStatements(null, RDF.type, inModel.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
                if(iter.hasNext()) {
                    Resource contextRes = iter.nextStatement().getSubject();
                    Statement isStringStm = contextRes.getProperty(inModel.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString"));
                    textForProcessing = isStringStm.getObject().asLiteral().getString();
                }
            }
            try {
                outModel = entityAPI.callDBpediaSpotlight(textForProcessing, confidenceParam, languageParam);
            }
            catch (BadRequestException e) {
				logger.error("failed", e);
				throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
			} catch (eu.freme.eservices.eentity.exceptions.ExternalServiceFailedException e) {
				logger.error("failed", e);
				throw new ExternalServiceFailedException();
			}
            
            // get output format
            RDFConstants.RDFSerialization outputFormat = getOutputSerialization(acceptHeader);
            try {
		Model model = rdfConversionService.unserializeRDF(outModel,
		RDFConstants.RDFSerialization.TURTLE);
                String serialization = rdfConversionService.serializeRDF(model, outputFormat);
		return new ResponseEntity<String>(serialization, HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
}
