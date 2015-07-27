package eu.freme.broker.security.database;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    User findOneByName(String name);
    
}
