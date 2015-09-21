package eu.freme.broker.security.database.dao;

import eu.freme.broker.security.database.model.Dataset;
import eu.freme.broker.security.database.model.OwnedResource;
import eu.freme.broker.security.database.model.User;
import eu.freme.broker.security.database.repository.DatasetRepository;
import eu.freme.broker.security.database.repository.OwnedResourceRepository;
import eu.freme.broker.security.tools.AccessLevelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.TransactionRequiredException;

/**
 * Created by Arne on 18.09.2015.
 */
public class OwnedResourceDAO<Entity extends OwnedResource>  extends DAO<OwnedResourceRepository<Entity>, Entity>{

    @Autowired
    AbstractAccessDecisionManager decisionManager;

    @Autowired
    AccessLevelHelper accessLevelHelper;

    public void delete(Entity entity){
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        User authUser = (User) authentication.getPrincipal();
        if(!authUser.getRole().equals(User.roleAdmin))
            decisionManager.decide(authentication, entity, accessLevelHelper.writeAccess());
        super.delete(entity);
    }


    public void save(Entity entity){
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        User authUser = (User) authentication.getPrincipal();
        if(!authUser.getRole().equals(User.roleAdmin))
            decisionManager.decide(authentication, entity, accessLevelHelper.writeAccess());
        super.save(entity);
    }

    public Entity findOneById(String id){
        Entity result = repository.findOneById(id);
        if(result==null)
            return null;
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        User authUser = (User) authentication.getPrincipal();
        if(!authUser.getRole().equals(User.roleAdmin))
            decisionManager.decide(authentication, result, accessLevelHelper.readAccess());
        return result;
    }

}
