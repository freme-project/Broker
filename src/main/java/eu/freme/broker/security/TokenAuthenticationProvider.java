package eu.freme.broker.security;

import com.google.common.base.Optional;

import eu.freme.broker.security.database.Token;
import eu.freme.broker.security.database.User;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class TokenAuthenticationProvider implements AuthenticationProvider {

	private TokenService tokenService;

	public TokenAuthenticationProvider(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Optional<String> token = (Optional) authentication.getPrincipal();
		if (!token.isPresent() || token.get().isEmpty()) {
			throw new BadCredentialsException("Invalid token");
		}

		Token tokenObject = tokenService.retrieve(token.get());
		if (tokenObject == null) {
			throw new BadCredentialsException("Invalid token");
		}

		User user = tokenObject.getUser();
		return new AuthenticationWithToken(user, null,
				AuthorityUtils.commaSeparatedStringToAuthorityList(user
						.getRole()), tokenObject);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(PreAuthenticatedAuthenticationToken.class);
	}
}
