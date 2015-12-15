package eu.freme.broker.eservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.common.exception.FREMEHttpException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.dao.FilterDAO;
import eu.freme.common.persistence.dao.UserDAO;
import eu.freme.common.persistence.model.Filter;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.User;
import eu.freme.common.persistence.repository.FilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by Arne on 11.12.2015.
 */
@RestController
@Profile("broker")
public class FilterController extends BaseRestController {

    @Autowired
    FilterDAO filterDAO;

    @Autowired
    UserDAO userDAO;

    @RequestMapping(value = "/toolbox/filter/documents/{filterName}", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> filter(
            @PathVariable("filterName") String filterName,
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
                    .contentType());
            return new ResponseEntity<>(serialization, responseHeaders,
                    HttpStatus.OK);

        }catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }


    @RequestMapping(value = "/toolbox/filter/manage/{filterName}", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> addFilterByName(
            @PathVariable("filterName") String filterName,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestBody String postBody
    ){
        try {

            Filter filter = ((FilterRepository)filterDAO.getRepository()).findOneByName(filterName);
            if(filter!=null)
                throw new FREMEHttpException("Can not add filter: Filter with name: "+filterName+" already exists.");

            filter = new Filter(OwnedResource.Visibility.getByString(visibility), filterName, postBody);
            filter = filterDAO.save(filter);

            HttpHeaders responseHeaders = new HttpHeaders();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String serialization = ow.writeValueAsString(filter);
            responseHeaders.add("Content-Type", RDFConstants.RDFSerialization.JSON.contentType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
        }catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @RequestMapping(value = "/toolbox/filter/manage/{filterName}", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> getFilterByName(
            @PathVariable("filterName") String filterName
    ){
        try {
            Filter filter = filterDAO.findOneByName(filterName);
            HttpHeaders responseHeaders = new HttpHeaders();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String serialization = ow.writeValueAsString(filter);
            responseHeaders.add("Content-Type", RDFConstants.RDFSerialization.JSON.contentType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
        }catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @RequestMapping(value = "/toolbox/filter/manage/{filterName}", method = RequestMethod.PUT)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> putFilterByName(
            @PathVariable("filterName") String filterName,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "newOwner", required = false) String ownerName,
            @RequestBody String postBody
    ){
        try {
            Filter filter = filterDAO.findOneByName(filterName);

            if(!Strings.isNullOrEmpty(postBody)){
                filter.setQuery(postBody);
            }

            if(!Strings.isNullOrEmpty(visibility)){
                filter.setVisibility(OwnedResource.Visibility.getByString(visibility));
            }

            filterDAO.save(filter);

            if (!Strings.isNullOrEmpty(ownerName)) {
                User owner = userDAO.getRepository().findOneByName(ownerName);
                if (owner == null)
                    throw new BadRequestException(
                            "Can not change owner of the template. User \""
                                    + ownerName + "\" does not exist.");
                filter = filterDAO.updateOwner(filter, owner);
            }
            HttpHeaders responseHeaders = new HttpHeaders();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String serialization = ow.writeValueAsString(filter);
            responseHeaders.add("Content-Type", RDFConstants.RDFSerialization.JSON.contentType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
        }catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @RequestMapping(value = "/toolbox/filter/manage/{filterName}", method = RequestMethod.DELETE)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> deleteFilterByName(
            @PathVariable("filterName") String filterName
    ){
        try {
            Filter filter = filterDAO.findOneByName(filterName);
            filterDAO.delete(filter);
            return new ResponseEntity<>("The template was sucessfully removed.", HttpStatus.OK);
        }catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @RequestMapping(value = "/toolbox/filter/manage/", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> getAllFilter(
    ){
        try {
            List<Filter> filters = filterDAO.findAllReadAccessible();
            HttpHeaders responseHeaders = new HttpHeaders();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String serialization = ow.writeValueAsString(filters);
            responseHeaders.add("Content-Type", RDFConstants.RDFSerialization.JSON.contentType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
        }catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }



}
