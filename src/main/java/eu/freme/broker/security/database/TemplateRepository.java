package eu.freme.broker.security.database;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */
public interface TemplateRepository extends CrudRepository<Template, Long> {

    Template findOneById(String name);
    
}
