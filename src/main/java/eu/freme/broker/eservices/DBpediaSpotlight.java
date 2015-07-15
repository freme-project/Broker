package eu.freme.broker.eservices;

import io.swagger.annotations.*;
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
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.eservices.eentity.api.EEntityService;
import eu.freme.eservices.eentity.exceptions.BadRequestException;

import java.io.ByteArrayInputStream;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@Api(value="e-Entity")
public class DBpediaSpotlight extends BaseRestController {

	@Autowired
	EEntityService entityAPI;

	@RequestMapping(value = "/e-entity/dbpedia-spotlight/documents", method = {
            RequestMethod.POST, RequestMethod.GET })
	

	 @ApiOperation(value = "Entity recognition and linking usind DBPedia-Spotlight engine.",
	    notes = "Entity enrichment with DBPedia-Spotlight engine",
	    responseContainer = "List")
	 @ApiResponses(value = { @ApiResponse(code = 400, message = "Insert message"),
	    @ApiResponse(code = 404, message = "Insert message") })
	
	
	
	public ResponseEntity<String> execute(
			
			@ApiParam(value="Plaintext sent as value of the input parameter. Short form is i.")
			@RequestParam(value = "input", required = false) String input,
			@ApiParam(name="HIDDEN") @RequestParam(value = "i", required = false) String i,
			
			@ApiParam(value="Format of input string. Only \"text\" is provided (default). Overrides Content-Type header. Short form is f.")
			@RequestParam(value = "informat", required = false) String informat,
			@ApiParam(name="HIDDEN") @RequestParam(value = "f", required = false) String f,
			
			@ApiParam("RDF serialization format of Output. Can be \"json-ld\", \"turtle\" (?). Defaults to \"turtle\". Overrides Accept Header. Short form is o.")
			@RequestParam(value = "outformat", required = false) String outformat,
			@ApiParam(name="HIDDEN") @RequestParam(value = "o", required = false) String o,
			
			@ApiParam("Unused optional Parameter. Short form is p.")
			@RequestParam(value = "prefix", required = false) String prefix,
			@ApiParam(name="HIDDEN") @RequestParam(value = "p", required = false) String p,
			
			@ApiParam(value="Format of outputg. Can be \"plaintext\", \"json-ld\", \"turtle\". Defaults to \"turtle\". ")
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			
			@ApiParam(value="Format of input string. Can be \"plaintext\", \"json-ld\", \"turtle\". Defaults to \"turtle\". ")
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			
			@ApiParam(value="Source language. Can be en,de,nl,fr,it,es (according to supported NER engine).")
			@RequestParam(value = "language", required = false) String languageParam,
			
			@ApiParam(value="Threshhold to limit the output of entities. Default is 0.3")
			@RequestParam(value = "confidence", required = false) String confidenceParam,
                        @RequestBody(required = false) String postBody) {
            
            NIFParameterSet parameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
           
            Model inModel;
            Model outModel = ModelFactory.createDefaultModel();;

            // merge long and short parameters - long parameters override short parameters
            if( input == null ){
                input = i;
            }
            if( informat == null ){
                informat = f;
            }
            if( outformat == null ){
                outformat = o;
            }
            if( prefix == null ){
                prefix = p;
            }
            
            String textForProcessing = null;
            
            if (parameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
                // input is sent as value of the input parameter
                textForProcessing = input;
            } else {
                // input is sent as body of the request
                inModel = ModelFactory.createDefaultModel();
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
                
                StmtIterator iter = inModel.listStatements(null, RDF.type, inModel.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
                if(iter.hasNext()) {
                    Resource contextRes = iter.nextStatement().getSubject();
                    Statement isStringStm = contextRes.getProperty(inModel.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString"));
                    textForProcessing = isStringStm.getObject().asLiteral().getString();                    
                }
                
                if(textForProcessing == null) {
                    throw new eu.freme.broker.exception.BadRequestException("No text to process could be found in the input.");
                }
            }
            
            try {
                String dbpediaSpotlightRes = entityAPI.callDBpediaSpotlight(textForProcessing, confidenceParam, languageParam, parameters.getPrefix());
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
                switch(parameters.getOutformat()) {
                    case TURTLE:
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.TURTLE);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);                
                    case JSON_LD:
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.JSON_LD);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                    case RDF_XML:
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.RDF_XML);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                    case N_TRIPLES:
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.N_TRIPLES);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                    case N3:
                        serialization = rdfConversionService.serializeRDF(outModel, RDFConstants.RDFSerialization.N3);
                        return new ResponseEntity<String>(serialization, HttpStatus.OK);
                }
            } catch (Exception e) {
                logger.error("failed", e);
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
