package eu.freme.broker.eservices;

import eu.freme.broker.tools.NIFParameterFactory;
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

@Api("e-Entity")

public class FremeNER extends BaseRestController {

	@Autowired
	EEntityService entityAPI;

	@ApiOperation(value = "Entity recognition and linking using Freme-NER engine.",
	   notes = "Enriches Text content with entities gathered by the Freme-NER engine. The service also accepts text sent as NIF document. The text of the nif:isString property (attached to the nif:Context document) will be used for processing.")
	@ApiResponses(value = {
       @ApiResponse(code = 200, message = "Successful response"),
	   @ApiResponse(code = 404, message = "Bad request - input validation failed") })
    // Submitting document for processing.
    @RequestMapping(value = "/e-entity/freme-ner/documents",
            method = {RequestMethod.POST, RequestMethod.GET },
            consumes = {"text/plain", "text/turtle", "application/json+ld", "application/n-triples", "application/rdf+xml", "text/n3"},
            produces = {"text/turtle", "application/json+ld", "application/n-triples", "application/rdf+xml", "text/n3"})
	public ResponseEntity<String> execute(
			@ApiParam(value="The text to enrich. Can be either plaintext or NIF (see parameter informat). Short form is i.")
			@RequestParam(value = "input", required = false) String input,
			@ApiParam("HIDDEN") @RequestParam(value = "i", required = false) String i,
			
			@ApiParam(value="Format of input string. Can be "+ NIFParameterFactory.allowedValuesInformat+". Overrides Content-Type header. Short form is f.",
                    allowableValues = NIFParameterFactory.allowedValuesInformat,
                    defaultValue = "text")
			@RequestParam(value = "informat", required = false) String informat,
			@ApiParam("HIDDEN") @RequestParam(value = "f", required = false) String f,
			
			@ApiParam(value="RDF serialization format of Output. Can be "+NIFParameterFactory.allowedValuesOutformat+". Overrides Accept Header (Response Content Type). Short form is o.",
                    allowableValues = NIFParameterFactory.allowedValuesOutformat,
                    defaultValue = "turtle")
			@RequestParam(value = "outformat", required = false) String outformat,
			@ApiParam("HIDDEN") @RequestParam(value = "o", required = false) String o,
			
			@ApiParam("Unused optional Parameter. Short form is p.")
			@RequestParam(value = "prefix", required = false) String prefix,
			@ApiParam("HIDDEN") @RequestParam(value = "p", required = false) String p,

            @RequestHeader(value = "Accept", required = false) String acceptHeader,

            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,

            @ApiParam(value="The text to enrich. Will be overwritten by parameter input, if set. The format of the body can be "+NIFParameterFactory.allowedValuesInformatMime+". Defaults to \"text/plain\". The parameter *informat* overrides the Content-Type.")
            @RequestBody(required = false) String postBody,

            @ApiParam(value="Source language. Can be en, de, nl, fr, it, es (according to supported NER engine).",
                    allowableValues = "en,de,nl,fr,it,es")
			@RequestParam(value = "language", required = false) String language,
			
			@ApiParam(value="A mandatory parameter which indicates the dataset used for entity linking which includes a list of entites and associated labels.")
			@RequestParam(value = "dataset", required = true) String dataset) {
            
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
    @ApiOperation(value = "Submitting dataset for use in the e-Entity service",
    notes = "Create dataset in SKOS format which includes prefLabel, altLabel or label properties (unless the param properties is explicitly set).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful response"),
            @ApiResponse(code = 404, message = "Bad request - input validation failed") })
	@RequestMapping(value = "/e-entity/freme-ner/datasets",
            method = {RequestMethod.POST },
            consumes = {"text/turtle", "application/json+ld", "application/n-triples", "application/rdf+xml", "text/n3"})
	public ResponseEntity<String> createDataset(
            @ApiParam(value="RDF serialization format of the dataset. Can be json+ld, turtle. Overrides Content-Type header. Short form is f.",
                    allowableValues = "turtle, json+ld",
                    defaultValue = "turtle")
            @RequestParam(value = "informat", required = false) String informat,
            @ApiParam("HIDDEN") @RequestParam(value = "f", required = false) String f,

            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,

            @ApiParam(value="The dataset. The format of the body can be \"text/turtle\", \"application/json+ld\", \"application/n-triples\", \"application/rdf+xml\", \"text/n3\". Defaults to \"text/turtle\". The parameter *informat* overrides the Content-Type.")
            @RequestBody(required = false) String postBody,

            @ApiParam("proposed dataset name. It can be considered as ID for the dataset. It should include only numbers, letters and should NOT include white spaces.")
            @RequestParam(value = "name", required = false) String name,

            @ApiParam(value = "language of the labels in the dataset. If the parameter is not specified, all labels without language tag will be used while performing linking. At the moment only following languages are supported - FREME NER (en/de/fr/es/it), DBpedia Spotlight (en).",
                    allowableValues = "de, en, nl, it, fr, es")
            @RequestParam(value = "language", required = false) String language) {
            
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
    @ApiOperation("Updating dataset for use in the e-Entity service")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful response"),
            @ApiResponse(code = 404, message = "Bad request - input validation failed") })
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}",
            method = {RequestMethod.PUT },
            consumes = {"text/turtle", "application/json+ld", "application/n-triples", "application/rdf+xml", "text/n3"})
	public ResponseEntity<String> updateDataset(
            @ApiParam(value="RDF serialization format of the dataset. Can be json+ld, turtle. Overrides Content-Type header. Short form is f.",
                    allowableValues = "turtle, json+ld",
                    defaultValue = "turtle")
            @RequestParam(value = "informat", required = false) String informat,
            @ApiParam("HIDDEN") @RequestParam(value = "f", required = false) String f,

            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,

            @ApiParam(value="The dataset. The format of the body can be \"text/turtle\", \"application/json+ld\", \"application/n-triples\", \"application/rdf+xml\", \"text/n3\". Defaults to \"text/turtle\". The parameter *informat* overrides the Content-Type.")
            @RequestBody(required = false) String postBody,

            @ApiParam("The name name of the dataset to update. It can be considered as ID for the dataset. It should include only numbers, letters and should NOT include white spaces.")
            @RequestParam(value = "name", required = false) String name,

            @ApiParam(value = "language of the labels in the dataset. If the parameter is not specified, all labels without language tag will be used while performing linking. At the moment only following languages are supported - FREME NER (en/de/fr/es/it), DBpedia Spotlight (en).",
                    allowableValues = "de, en, nl, it, fr, es")
            @RequestParam(value = "language", required = false) String language) {
            
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
	@ApiOperation("Get info about a specific dataset")
    @RequestMapping(value = "/e-entity/freme-ner/datasets/{name}",
            method = {RequestMethod.GET })
	public ResponseEntity<String> getDataset(
            @ApiParam("The name of teh requested dataset.")
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
    @ApiOperation("Get info about all available datasets")
	@RequestMapping(value = "/e-entity/freme-ner/datasets",
            method = {RequestMethod.GET })
	public ResponseEntity<String> getAllDatasets() {
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location",
                    "http://139.18.2.231:8080/api/datasets");
            return new ResponseEntity<String>(null,headers,HttpStatus.TEMPORARY_REDIRECT);
        }
        
        // Removing a specific dataset.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test" -X DELETE
    @ApiOperation("Removing a specific dataset")
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}",
            method = {RequestMethod.DELETE })
	public ResponseEntity<String> removeDataset(
            @ApiParam("The name of the dataset to remove")
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
