package eu.freme.broker.eservices;

import com.hp.hpl.jena.rdf.model.Model;
import eu.freme.broker.exception.FREMEHttpException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.common.persistence.dao.FilterDAO;
import eu.freme.common.persistence.model.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by Arne on 11.12.2015.
 */
public class SparqlFilter extends BaseRestController {

    @Autowired
    FilterDAO filterDAO;

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> enrich(
            @RequestParam(value = "filter-name") String filterName,
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams
    ){
        try {
            NIFParameterSet nifParameters = this.normalizeNif(postBody,
                    acceptHeader, contentTypeHeader, allParams, false);

            Filter filter = filterDAO.findOneByName(filterName);

            Model model = rdfConversionService.unserializeRDF(
                    nifParameters.getInput(), nifParameters.getInformat());

            model = filter.getFilteredModel(model);

            HttpHeaders responseHeaders = new HttpHeaders();
            String serialization = rdfConversionService.serializeRDF(model,
                    nifParameters.getOutformat());
            responseHeaders.add("Content-Type", nifParameters.getOutformat()
                    .getMimeType());
            return new ResponseEntity<>(serialization, responseHeaders,
                    HttpStatus.OK);

        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }

    }





}
