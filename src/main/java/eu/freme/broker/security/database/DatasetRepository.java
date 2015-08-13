package eu.freme.broker.security.database;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */
public interface DatasetRepository extends CrudRepository<Dataset, Long> {

    Dataset findOneById(String name);
    
}
