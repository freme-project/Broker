package eu.freme.broker.security.database;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */
public interface OwnedResourceRepository extends CrudRepository<OwnedResource, Long> {

    OwnedResource findOneByName(String name);
    
}
