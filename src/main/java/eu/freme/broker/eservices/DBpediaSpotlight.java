package eu.freme.broker.eservices;

import java.io.ByteArrayInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.freme.broker.exception.ExternalServiceFailedException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.eservices.eentity.api.EEntityService;
import eu.freme.eservices.eentity.exceptions.BadRequestException;

@RestController
public class DBpediaSpotlight extends BaseRestController {

	@Autowired
	EEntityService entityAPI;

	@RequestMapping(value = "/e-entity/dbpedia-spotlight/documents", method = {
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
			@RequestParam(value = "language", required = false) String languageParam,
			@RequestParam(value = "confidence", required = false) String confidenceParam,
                        @RequestBody(required = false) String postBody) {
            
            NIFParameterSet parameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
           
            Model inModel = ModelFactory.createDefaultModel();
            Model outModel = ModelFactory.createDefaultModel();;

            // Check the language parameter.
            if(languageParam == null) {
                throw new eu.freme.broker.exception.BadRequestException("Parameter language is not specified");            
            } else {
                if(languageParam.equals("en")) {
                    // OK, the language is supported.
                } else {
                    // The language specified with the langauge parameter is not supported.
                    throw new eu.freme.broker.exception.BadRequestException("Unsupported language ["+languageParam+"].");
                }
            }
            
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
                if(input == null) {
                    textForProcessing = postBody;
                } else {
                    textForProcessing = input;
                }
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
//                    System.out.println(tmpPrefix);
                    parameters.setPrefix(tmpPrefix+"#");
//                    System.out.println(parameters.getPrefix());
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
//            System.out.println("the prefix: "+parameters.getPrefix());
            try {
                
                validateConfidenceScore(confidenceParam);
                
                String dbpediaSpotlightRes = entityAPI.callDBpediaSpotlight(textForProcessing, confidenceParam, languageParam, parameters.getPrefix());
                outModel.read(new ByteArrayInputStream(dbpediaSpotlightRes.getBytes()), null, "TTL");
                outModel.add(inModel);
                // remove unwanted info
                outModel.removeAll(null, RDF.type, OWL.ObjectProperty);
                outModel.removeAll(null, RDF.type, OWL.DatatypeProperty);
                outModel.removeAll(null, RDF.type, OWL.Class);
                outModel.removeAll(null, RDF.type, OWL.Class);
                ResIterator resIter = outModel.listResourcesWithProperty(RDF.type, outModel.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/rlog#Entry"));
                while(resIter.hasNext()) {
                    Resource res = resIter.next();
                    outModel.removeAll(res, null, (RDFNode)null);
                }
            } catch (BadRequestException e) {
                logger.error("failed", e);
                throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
            } catch (eu.freme.eservices.eentity.exceptions.ExternalServiceFailedException e) {
                logger.error("failed", e);
                throw new ExternalServiceFailedException();
            }
            
            return createSuccessResponse(outModel, parameters.getOutformat());
        }

    private void validateConfidenceScore(String confidenceParam) {
        if(confidenceParam == null)
            return;
        double confVal = Double.parseDouble(confidenceParam);
        if(confVal >= .00 && confVal <= 1.0) {
            // the conf value is OK.
        } else {
            throw new BadRequestException("The value of the confidence parameter is out of the range [0..1].");
        }
    }
}
