package eu.freme.broker.eservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.FREMEHttpException;
import eu.freme.common.persistence.dao.OwnedResourceDAO;
import eu.freme.common.persistence.dao.UserDAO;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by Arne on 07.01.2016.
 */
@RestController
public abstract class RestrictedResourceManagingController<Entity extends OwnedResource> extends BaseRestController {

    protected abstract Entity createEntity(String id, OwnedResource.Visibility visibility, String description, String body, Map<String, String> parameters) throws AccessDeniedException;
    protected abstract void updateEntity(Entity entity, String body, Map<String, String> parameters);

    @Autowired
    OwnedResourceDAO<Entity> entityDAO;

    @Autowired
    UserDAO userDAO;

    //@Autowired
    //JenaRDFConversionService jenaRDFConversionService;

    @RequestMapping(value = "/manage", method = RequestMethod.POST)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> addEntity(
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam Map<String, String> allParams,
            @RequestBody String postBody
    ){
        try {

            if(!Strings.isNullOrEmpty(entityId)) {
                Entity entity = entityDAO.findOneByIdUnchecked(entityId);
                if (entity != null)
                    throw new FREMEHttpException("Can not add entity: Entity with identifier: " + entityId + " already exists.");
            }
            Entity entity = createEntity(entityId, OwnedResource.Visibility.getByString(visibility), description, postBody, allParams);
            entity = entityDAO.save(entity);

            HttpHeaders responseHeaders = new HttpHeaders();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String serialization = ow.writeValueAsString(entity);
            responseHeaders.add("Content-Type", RDFSerialization.JSON.contentType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
        } catch (AccessDeniedException ex) {
            logger.error(ex.getMessage(), ex);
            throw new eu.freme.broker.exception.AccessDeniedException(
                    ex.getMessage());
        } catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (org.json.JSONException ex) {
            logger.error(ex.getMessage(), ex);
            throw new BadRequestException(ex.getMessage());
        }catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @RequestMapping(value = "/manage/{filterName}", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> getEntityById(
            @PathVariable("filterName") String filterName
    ){
        try {
            Entity entity = entityDAO.findOneById(filterName);
            HttpHeaders responseHeaders = new HttpHeaders();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String serialization = ow.writeValueAsString(entity);
            responseHeaders.add("Content-Type", RDFSerialization.JSON.contentType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
        }catch (AccessDeniedException ex) {
            logger.error(ex.getMessage(), ex);
            throw new eu.freme.broker.exception.AccessDeniedException();
        } catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @RequestMapping(value = "/manage/{filterName}", method = RequestMethod.PUT)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> putEntityById(
            @PathVariable("filterName") String filterName,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "newOwner", required = false) String ownerName,
            @RequestParam Map<String, String> allParams,
            @RequestBody String postBody
    ){
        try {
            Entity entity = entityDAO.findOneById(filterName);

            updateEntity(entity, postBody, allParams);

            if(!Strings.isNullOrEmpty(visibility)){
                entity.setVisibility(OwnedResource.Visibility.getByString(visibility));
            }

            if(!Strings.isNullOrEmpty(description)){
                entity.setDescription(description);
            }

            entityDAO.save(entity);

            if (!Strings.isNullOrEmpty(ownerName)) {
                User owner = userDAO.getRepository().findOneByName(ownerName);
                if (owner == null)
                    throw new BadRequestException(
                            "Can not change owner of the entity. User \""
                                    + ownerName + "\" does not exist.");
                entity = entityDAO.updateOwner(entity, owner);
            }
            HttpHeaders responseHeaders = new HttpHeaders();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String serialization = ow.writeValueAsString(entity);
            responseHeaders.add("Content-Type", RDFSerialization.JSON.contentType());
            return new ResponseEntity<>(serialization, responseHeaders, HttpStatus.OK);
        }catch (AccessDeniedException ex) {
            logger.error(ex.getMessage(), ex);
            throw new eu.freme.broker.exception.AccessDeniedException();
        } catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (org.json.JSONException ex) {
            logger.error(ex.getMessage(), ex);
            throw new BadRequestException(
                    "The JSON object is incorrectly formatted. Problem description: "
                            + ex.getMessage());
        } catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @RequestMapping(value = "/manage/{filterName}", method = RequestMethod.DELETE)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> deleteEntityById(
            @PathVariable("filterName") String filterName
    ){
        try {
            Entity entity = entityDAO.findOneById(filterName);
            entityDAO.delete(entity);
            return new ResponseEntity<>("The entity was sucessfully removed.", HttpStatus.OK);
        }catch (AccessDeniedException ex) {
            logger.error(ex.getMessage(), ex);
            throw new eu.freme.broker.exception.AccessDeniedException();
        } catch (BadRequestException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (FREMEHttpException ex){
            logger.error(ex.getMessage());
            throw ex;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> getAllEntities(
    ){
        try {
            List<Entity> entities = entityDAO.findAllReadAccessible();
            HttpHeaders responseHeaders = new HttpHeaders();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String serialization = ow.writeValueAsString(entities);
            responseHeaders.add("Content-Type", RDFSerialization.JSON.contentType());
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
