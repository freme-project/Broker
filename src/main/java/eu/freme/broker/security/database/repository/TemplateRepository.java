package eu.freme.broker.security.database.repository;

import org.springframework.data.repository.CrudRepository;

import eu.freme.broker.security.database.model.Template;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */
public interface TemplateRepository extends CrudRepository<Template, Long> {

    Template findOneById(String name);
    
}
