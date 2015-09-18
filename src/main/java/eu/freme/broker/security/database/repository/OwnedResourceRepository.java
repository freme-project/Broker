package eu.freme.broker.security.database.repository;

import eu.freme.broker.security.database.model.OwnedResource;
import eu.freme.broker.security.database.model.Template;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Created by Arne on 18.09.2015.
 */

@NoRepositoryBean
public interface OwnedResourceRepository<T extends OwnedResource> extends CrudRepository<T, Long> {
    T findOneById(String name);
}
