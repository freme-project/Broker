package eu.freme.broker.security.token;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.freme.broker.security.database.Token;
import eu.freme.broker.security.database.TokenRepository;
import eu.freme.broker.security.database.User;

import java.util.Collection;
import java.util.UUID;

public class TokenService {

	private static final Logger logger = LoggerFactory
			.getLogger(TokenService.class);

	@Autowired
	TokenRepository tokenRepository;

	public Token generateNewToken(User user) {
		Token token = new Token(UUID.randomUUID().toString(), user);
		return token;
	}

	public Token retrieve(String tokenStr) {
		Token token = tokenRepository.findOneByToken(tokenStr);
		if (token == null) {
			throw new BadCredentialsException("invalid token");
		}
		return token;
	}
}
