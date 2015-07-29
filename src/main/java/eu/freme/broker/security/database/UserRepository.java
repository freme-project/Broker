package eu.freme.broker.security.database;

import org.springframework.data.repository.CrudRepository;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public interface UserRepository extends CrudRepository<User, Long> {

    User findOneByName(String name);
    
}
