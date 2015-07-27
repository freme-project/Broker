package eu.freme.broker.security;

import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.security.database.Token;
import eu.freme.broker.security.database.User;
import eu.freme.broker.security.database.UserRepository;
import eu.freme.broker.security.tools.PasswordHasher;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;

public class DatabaseAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	UserRepository userRepository;

	@Autowired
	TokenService tokenService;

	Logger logger = Logger.getLogger(DatabaseAuthenticationProvider.class);

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {

		String username = (String) authentication.getPrincipal();
		String password = (String) authentication.getCredentials();

		User user = userRepository.findOneByName(username);
		if (user == null) {
			throw new BadCredentialsException("authentication failed");
		}

		try {
			if (!PasswordHasher.check(password, user.getPassword())) {
				throw new BadCredentialsException("authentication failed");
			}
		} catch (BadCredentialsException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			throw new InternalServerErrorException();
		}

		Token token = tokenService.generateNewToken(user);

		AuthenticationWithToken auth = new AuthenticationWithToken(user, null,
				AuthorityUtils
						.commaSeparatedStringToAuthorityList(user.getRole()),
				token);
		return auth;

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
