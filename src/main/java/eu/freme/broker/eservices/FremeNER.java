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
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.eservices.eentity.api.EEntityService;
import eu.freme.eservices.eentity.exceptions.BadRequestException;

import java.io.ByteArrayInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestBody;


import io.swagger.annotations.*;
@RestController

@Api(value="e-Entity")

public class FremeNER extends BaseRestController {

	@Autowired
	EEntityService entityAPI;

        // Submitting document for processing.
	@RequestMapping(value = "/e-entity/freme-ner/documents", method = {
            RequestMethod.POST, RequestMethod.GET })
	

	 @ApiOperation(httpMethod="POST" , value = "Entity recognition and linking using Freme-NER engine.",
	    notes = "Entity enrichment with Freme-NER engine",
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
			@RequestParam(value = "language", required = false) String language,
			
			@ApiParam(value="A mandatory parameter which indicates the dataset used for entity linking which includes a list of entites and associated labels.")
			@RequestParam(value = "dataset", required = false) String dataset,
            @RequestBody(required = false) String postBody) {
            
            // Check the language parameter.
            if(language == null) {
                throw new eu.freme.broker.exception.BadRequestException("Parameter language is not specified");            
            } else {
                if(language.equals("en") 
                        || language.equals("de") 
                        || language.equals("nl")
                        || language.equals("it")
                        || language.equals("fr")
                        || language.equals("es")) {
                    // OK, the language is supported.
                } else {
                    // The language specified with the langauge parameter is not supported.
                    throw new eu.freme.broker.exception.BadRequestException("Unsupported language.");
                }
            }
            
            // Check the dataset parameter.
            if(dataset == null) {
                throw new eu.freme.broker.exception.BadRequestException("Dataset language is not specified");            
            } else {
                // OK, dataset specified.
            }
            
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
                textForProcessing = parameters.getInput();
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
                String fremeNERRes = entityAPI.callFremeNER(textForProcessing, language, parameters.getPrefix(), dataset);
                outModel.read(new ByteArrayInputStream(fremeNERRes.getBytes()), null, "TTL");
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

        // Submitting dataset for use in the e-Entity service.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/?name=test&language=en" -X POST
	@RequestMapping(value = "/e-entity/freme-ner/datasets", method = {
            RequestMethod.POST })
	public ResponseEntity<String> createDataset(
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
                        @RequestBody(required = false) String postBody) {
            
            // merge long and short parameters - long parameters override short parameters.
            if( informat == null ){
                informat = f;
            }
            // Check the dataset name parameter.
            if(name == null) {
                throw new eu.freme.broker.exception.BadRequestException("Parameter name is not specified");            
            }
            // Check the language parameter.
            if(language == null) {
                throw new eu.freme.broker.exception.BadRequestException("Parameter language is not specified");            
            } else {
                if(language.equals("en") 
                        || language.equals("de") 
                        || language.equals("nl")
                        || language.equals("it")
                        || language.equals("fr")
                        || language.equals("es")) {
                    // OK, the language is supported.
                } else {
                    // The language specified with the langauge parameter is not supported.
                    throw new eu.freme.broker.exception.BadRequestException("Unsupported language.");
                }
            }
            // Check if data was sent.
            if( postBody == null || postBody.trim().length() == 0 ){
                throw new eu.freme.broker.exception.BadRequestException("No data to process could be found in the input.");
            }
            
            // Checking the informat parameter
            RDFConstants.RDFSerialization thisInformat;
            if (informat == null && contentTypeHeader == null) {
                thisInformat = RDFConstants.RDFSerialization.TURTLE;
            } else if (informat != null) {
                if (!rdfSerializationFormats.containsKey(informat)) {
                    throw new eu.freme.broker.exception.BadRequestException( "The parameter informat has invalid value \"" + informat + "\"");
                }
                thisInformat = rdfSerializationFormats.get(informat);
            } else {
                if (!rdfSerializationFormats.containsKey(contentTypeHeader)) {
                    throw new eu.freme.broker.exception.BadRequestException("Content-Type header has invalid value \"" + contentTypeHeader + "\"");
                }
                thisInformat = rdfSerializationFormats.get(contentTypeHeader);
            }
            // END: Checking the informat parameter
            
            String format = null;
            HttpHeaders headers = new HttpHeaders();
            switch(thisInformat) {
                case TURTLE:
                    format = "TTL";
                    break;
                case JSON_LD:
                    format = "JSON-LD";
                    break;
                case RDF_XML:
                    format = "RDF/XML";
                    break;
                case N_TRIPLES:
                    format = "N-TRIPLES";
                    break;
                case N3:
                    format = "N3";
                    break;
            }
            
            headers.add("Location",
                    "http://139.18.2.231:8080/api/datasets?format="+format
                    + "&name="+name
                    + "&language="+language);
            return new ResponseEntity<String>(null,headers,HttpStatus.TEMPORARY_REDIRECT);
        }
        
        // Updating dataset for use in the e-Entity service.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test?language=en" -X PUT
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = {
            RequestMethod.PUT })
	public ResponseEntity<String> updateDataset(
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
			@PathVariable(value = "name") String name,
			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
                        @RequestBody(required = false) String postBody) {
            
            // Check the dataset name parameter.
            if(name == null) {
                throw new eu.freme.broker.exception.BadRequestException("Unspecified dataset name.");            
            }
            // Check the language parameter.
            if(language == null) {
                throw new eu.freme.broker.exception.BadRequestException("Unspecified dataset language.");            
            } else {
                if(language.equals("en") 
                        || language.equals("de") 
                        || language.equals("nl")
                        || language.equals("it")
                        || language.equals("fr")
                        || language.equals("es")) {
                    // OK, the language is supported.
                } else {
                    // The language specified with the langauge parameter is not supported.
                    throw new eu.freme.broker.exception.BadRequestException("Unsupported language.");
                }
            }
            // Check if data was sent.
            if( postBody == null || postBody.trim().length() == 0 ){
                throw new eu.freme.broker.exception.BadRequestException("No data to process could be found in the input.");
            }
            // Checking the informat parameter
            RDFConstants.RDFSerialization thisInformat;
            if (informat == null && contentTypeHeader == null) {
                thisInformat = RDFConstants.RDFSerialization.TURTLE;
            } else if (informat != null) {
                if (!rdfSerializationFormats.containsKey(informat)) {
                    throw new eu.freme.broker.exception.BadRequestException( "The parameter informat has invalid value \"" + informat + "\"");
                }
                thisInformat = rdfSerializationFormats.get(informat);
            } else {
                if (!rdfSerializationFormats.containsKey(contentTypeHeader)) {
                    throw new eu.freme.broker.exception.BadRequestException("Content-Type header has invalid value \"" + contentTypeHeader + "\"");
                }
                thisInformat = rdfSerializationFormats.get(contentTypeHeader);
            }
            // END: Checking the informat parameter
            
            String format = null;
            HttpHeaders headers = new HttpHeaders();
            switch(thisInformat) {
                case TURTLE:
                    format = "TTL";
                    break;
                case JSON_LD:
                    format = "JSON-LD";
                    break;
                case RDF_XML:
                    format = "RDF/XML";
                    break;
                case N_TRIPLES:
                    format = "N-TRIPLES";
                    break;
                case N3:
                    format = "N3";
                    break;
            }
            headers.add("Location",
                    "http://139.18.2.231:8080/api/datasets"+name+"?format="+format
                    + "&language="+language);
            
            return new ResponseEntity<String>(null,headers,HttpStatus.TEMPORARY_REDIRECT);
        }
        
        // Get info about a specific dataset.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = {
            RequestMethod.GET })
	public ResponseEntity<String> getDataset(
                @PathVariable(value = "name") String name) {
            
            // Check the dataset name parameter.
            if(name == null) {
                throw new eu.freme.broker.exception.BadRequestException("Unspecified dataset name.");            
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Location",
                    "http://139.18.2.231:8080/api/datasets/"+name);
            return new ResponseEntity<String>(null,headers,HttpStatus.TEMPORARY_REDIRECT);
        }
        
        // Get info about all available datasets.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets
	@RequestMapping(value = "/e-entity/freme-ner/datasets", method = {
            RequestMethod.GET })
	public ResponseEntity<String> getAllDatasets() {
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location",
                    "http://139.18.2.231:8080/api/datasets");
            return new ResponseEntity<String>(null,headers,HttpStatus.TEMPORARY_REDIRECT);
        }
        
        // Removing a specific dataset.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test" -X DELETE
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = {
            RequestMethod.DELETE })
	public ResponseEntity<String> removeDataset(
			@PathVariable(value = "name") String name) {
            
            // Check the dataset name parameter.
            if(name == null) {
                throw new eu.freme.broker.exception.BadRequestException("Unspecified dataset name.");            
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location",
                    "http://139.18.2.231:8080/api/datasets/"+name);
            return new ResponseEntity<String>(null,headers,HttpStatus.TEMPORARY_REDIRECT);
        }
}
