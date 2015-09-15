package eu.freme.broker.security.database.repository;

import org.springframework.data.repository.CrudRepository;

import eu.freme.broker.security.database.model.Dataset;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */
public interface DatasetRepository extends CrudRepository<Dataset, Long> {

    Dataset findOneById(String name);
    
}
