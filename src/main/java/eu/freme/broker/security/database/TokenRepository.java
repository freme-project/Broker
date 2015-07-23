package eu.freme.broker.security.database;


import org.springframework.data.repository.CrudRepository;


public interface TokenRepository extends CrudRepository<Token, Long> {

	public Token findOneByToken(String token);
}
