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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.freme.broker.exception.ExternalServiceFailedException;
import eu.freme.broker.security.database.model.Dataset;
import eu.freme.broker.security.database.model.OwnedResource;
import eu.freme.broker.security.database.model.User;
import eu.freme.broker.security.database.repository.DatasetRepository;
import eu.freme.broker.security.database.repository.UserRepository;
import eu.freme.broker.security.tools.AccessLevelHelper;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.eservices.eentity.api.EEntityService;
import eu.freme.eservices.eentity.exceptions.BadRequestException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class FremeNER extends BaseRestController {

	@Autowired
	EEntityService entityAPI;

    @Autowired
    AbstractAccessDecisionManager decisionManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DatasetRepository datasetRepository;

    @Autowired
    AccessLevelHelper accessLevelHelper;

        // Submitting document for processing.
	@RequestMapping(value = "/e-entity/freme-ner/documents", method = {
            RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> execute(
			@RequestParam(value = "input", required = false) String input,
			@RequestParam(value = "i", required = false) String i,
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
			@RequestParam(value = "outformat", required = false) String outformat,
			@RequestParam(value = "o", required = false) String o,
			@RequestParam(value = "prefix", required = false) String prefix,
			@RequestParam(value = "p", required = false) String p,
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "dataset", required = false) String dataset,
			@RequestParam(value = "numLinks", required = false) String numLinksParam,
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
            
            int numLinks = 1;
            // Check the dataset parameter.
            if(numLinksParam != null) {
                numLinks = Integer.parseInt(numLinksParam);
                if(numLinks > 5) {
                    numLinks = 1;
                }
            }
            
            NIFParameterSet parameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
           
            Model inModel = ModelFactory.createDefaultModel();
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
                
                boolean textFound = false;
                String tmpPrefix = "http://freme-project.eu/#";
                // The first nif:Context with assigned nif:isString will be processed.
                while(!textFound) {
                    Resource contextRes = iter.nextStatement().getSubject();
                    tmpPrefix = contextRes.getURI().split("#")[0];
                    parameters.setPrefix(tmpPrefix+"#");
                    Statement isStringStm = contextRes.getProperty(inModel.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString"));
                    if(isStringStm != null) {
                        textForProcessing = isStringStm.getObject().asLiteral().getString();
                        textFound = true;
                    }                    
                }
                
                if(textForProcessing == null) {
                    throw new eu.freme.broker.exception.BadRequestException("No text to process.");
                }
            }
            
            try {
                String fremeNERRes = entityAPI.callFremeNER(textForProcessing,language,parameters.getPrefix(), dataset, numLinks);
                outModel.read(new ByteArrayInputStream(fremeNERRes.getBytes()), null, "TTL");
                outModel.add(inModel);
            } catch (BadRequestException e) {
                logger.error("failed", e);
                throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
            } catch (eu.freme.eservices.eentity.exceptions.ExternalServiceFailedException e) {
                logger.error("failed", e);
                throw new ExternalServiceFailedException();
            }
            
            return createSuccessResponse(outModel,  parameters.getOutformat());
        }

        // Submitting dataset for use in the e-Entity service.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/?name=test&language=en" -X POST
	@RequestMapping(value = "/e-entity/freme-ner/datasets", method = {
            RequestMethod.POST })
	public ResponseEntity<String> createDataset(
			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
			@RequestParam(value = "endpoint", required = false) String endpoint,
			@RequestParam(value = "sparql", required = false) String sparql,
                        @RequestBody(required = false) String postBody) {
            
            // merge long and short parameters - long parameters override short parameters.
            if( informat == null ){
                informat = f;
            }
            // Check the dataset name parameter.
            if(name == null) {
                throw new eu.freme.broker.exception.BadRequestException("Parameter name is not specified");            
            }
            // Check the dataset name parameter.
            if(description == null) {
                throw new eu.freme.broker.exception.BadRequestException("Parameter description is not specified");            
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
            // first check if user wants to submit data via SPARQL
            if(endpoint != null) {
                if(sparql != null) {
                    
                } else {
                    // endpoint specified, but not sparql => throw exception
                    throw new eu.freme.broker.exception.BadRequestException("SPARQL endpoint was specified but not a SPARQL query.");
                }
            }
            // if not, then check the body of the request
            else if( postBody == null || postBody.trim().length() == 0 ){
                // Check if data was sent.
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

            ResponseEntity<String> result = null;
            if(endpoint != null && sparql != null) {
                try {
                    // fed via SPARQL endpoint
                    result = callBackend("http://139.18.2.231:8080/api/datasets?format="+format
                            + "&name="+name
                            + "&description="+URLEncoder.encode(description,"UTF-8")
                            + "&language="+language
                            + "&endpoint="+endpoint
                            + "&sparql="+URLEncoder.encode(sparql,"UTF-8"), HttpMethod.POST, null);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(FremeNER.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                // datasets is sent
                result = callBackend("http://139.18.2.231:8080/api/datasets?format="+format
                    + "&name="+name
                    + "&language="+language, HttpMethod.POST, postBody);
            }
            if(result!= null && result.getStatusCode().is2xxSuccessful()) {
                Authentication authentication = SecurityContextHolder.getContext()
                        .getAuthentication();
                User user = (User) authentication.getPrincipal();

                Dataset dataset = new Dataset(name, user, OwnedResource.Visibility.PRIVATE);
                datasetRepository.save(dataset);
            }
            return result;
        }
        
        // Updating dataset for use in the e-Entity service.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test?language=en" -X PUT
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = {
            RequestMethod.PUT })
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
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

            return callBackend("http://139.18.2.231:8080/api/datasets"+name+"?format="+format
                    + "&language="+language, HttpMethod.PUT, postBody);
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

            return callBackend("http://139.18.2.231:8080/api/datasets/"+name, HttpMethod.GET, null);
        }

        // Get info about all available datasets.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets
	@RequestMapping(value = "/e-entity/freme-ner/datasets", method = {
            RequestMethod.GET })
	public ResponseEntity<String> getAllDatasets() {

            return callBackend("http://139.18.2.231:8080/api/datasets", HttpMethod.GET, null);
        }
        
        // Removing a specific dataset.
        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test" -X DELETE
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = {
            RequestMethod.DELETE })
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> removeDataset(
			@PathVariable(value = "name") String name) {

            // Check the dataset name parameter.
            if(name == null) {
                throw new eu.freme.broker.exception.BadRequestException("Unspecified dataset name.");            
            }

            Authentication authentication = SecurityContextHolder.getContext()
                    .getAuthentication();

            Dataset dataset = datasetRepository.findOneById(name);
            decisionManager.decide(authentication, dataset, accessLevelHelper.writeAccess());


            ResponseEntity<String> result = callBackend("http://139.18.2.231:8080/api/datasets/"+name, HttpMethod.DELETE, null);
            if(result.getStatusCode().is2xxSuccessful()) {
                datasetRepository.delete(dataset);
            }
            return result;
        }


    private ResponseEntity<String> callBackend(String uri, HttpMethod method, String body) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            if(body == null) {
                return restTemplate.exchange(new URI(uri), method, null, String.class);
            } else {
                return restTemplate.exchange(new URI(uri), method, new HttpEntity<String>(body), String.class);
            }
        } catch (RestClientException rce) {
            logger.error("failed", rce);
            throw new eu.freme.broker.exception.ExternalServiceFailedException(rce.getMessage());
        } catch (Exception e) {
            logger.error("failed", e);
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
