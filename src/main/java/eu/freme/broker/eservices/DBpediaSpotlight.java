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
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.conversion.rdf.RDFConversionService;
import eu.freme.eservices.eentity.api.EEntityService;
import eu.freme.eservices.eentity.exceptions.BadRequestException;

import java.io.ByteArrayInputStream;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class DBpediaSpotlight extends BaseRestController {

	@Autowired
	EEntityService entityAPI;

	@Autowired
	RDFConversionService rdfConversionService;

	@RequestMapping(value = "/e-entity/dbpedia-spotlight", method = {
            RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> execute(
			@RequestParam(value = "input", required = false) String input,
			@RequestParam(value = "i", required = false) String i,
                        // text, text/turtle, application/json+ld
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
                        // text, text/turtle, application/json+ld
			@RequestParam(value = "outformat", required = false) String outformat,
			@RequestParam(value = "o", required = false) String o,
			@RequestParam(value = "prefix", required = false) String prefix,
			@RequestParam(value = "p", required = false) String p,
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			@RequestParam(value = "language", required = false) String languageParam,
			@RequestParam(value = "confidence", required = false) String confidenceParam,
                        @RequestBody(required = false) String postBody) {

            NIFParameterSet parameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);            
           
            Model inModel;
            Model outModel = ModelFactory.createDefaultModel();;

            String textForProcessing = null;
            
            // text, text/turtle, application/json+ld
            String readFrom = null;
            if(informat != null) {
                readFrom = informat;
            } else if(f != null) {
                readFrom = f;                
            } else if (contentTypeHeader != null) {
                readFrom = contentTypeHeader;
            } else {
                throw new BadRequestException("Neither informat/i param nor the Content-Type header were set.");
            }
                        
            if(readFrom.equals("text")) {
                // reading the output from the input parameter
                if(input != null) {
                    textForProcessing = input;
                // reading the output from the i (short form) parameter
                } else if (i != null) {
                    textForProcessing = i;
                } else {
                    // input and i param are not set
                    // return bad request
                    throw new BadRequestException("You set the informat param to text, however, neither input nor i param was set.");
                }                
            } else {
                // the content is sent as body
                if(contentTypeHeader == null) {
                    throw new BadRequestException("Neither informat nor Content-Type header was set.");
                }
            
                inModel = ModelFactory.createDefaultModel();
                
                switch(contentTypeHeader) {

                    case "text/turtle":
                        inModel.read(new ByteArrayInputStream(postBody.getBytes()), null, "TTL");
                        break;
                        
                    case "application/x-turtle":
                        inModel.read(new ByteArrayInputStream(postBody.getBytes()), null, "TTL");
                        break;
                        
                    case "application/json+ld":
                        inModel.read(new ByteArrayInputStream(postBody.getBytes()), null, "JSON-LD");
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
                String dbpediaSpotlightRes = entityAPI.callDBpediaSpotlight(textForProcessing, confidenceParam, languageParam);
                outModel.read(new ByteArrayInputStream(dbpediaSpotlightRes.getBytes()), null, "TTL");
            } catch (BadRequestException e) {
                logger.error("failed", e);
                throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
            } catch (eu.freme.eservices.eentity.exceptions.ExternalServiceFailedException e) {
                logger.error("failed", e);
                throw new ExternalServiceFailedException();
            }
            
            String serialization;
            try {
                serialization = rdfConversionService.serializeRDF(outModel, parameters.getOutformat());
            } catch (Exception e) {
                logger.error("failed", e);
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            return new ResponseEntity<String>(serialization, HttpStatus.OK);
        }
}
