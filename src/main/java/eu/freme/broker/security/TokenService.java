package eu.freme.broker.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import eu.freme.broker.security.database.Token;
import eu.freme.broker.security.database.TokenRepository;
import eu.freme.broker.security.database.User;

import java.util.Date;
import java.util.UUID;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class TokenService {

	@Autowired
	TokenRepository tokenRepository;

	public Token generateNewToken(User user) {
		Token token = new Token(UUID.randomUUID().toString(), user);
		tokenRepository.save(token);
		return token;
	}

	public Token retrieve(String tokenStr) {
		Token token = tokenRepository.findOneByToken(tokenStr);
		if (token == null) {
			throw new BadCredentialsException("invalid token");
		}
		return token;
	}
	
	public Token updateLastUsed(Token token){
		token.setLastUsedDate(new Date());
		tokenRepository.save(token);
		return token;
	}
}
