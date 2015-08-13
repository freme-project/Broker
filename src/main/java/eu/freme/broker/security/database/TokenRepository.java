package eu.freme.broker.security.database;


import org.springframework.data.repository.CrudRepository;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public interface TokenRepository extends CrudRepository<Token, Long> {

	Token findOneByToken(String token);
}
