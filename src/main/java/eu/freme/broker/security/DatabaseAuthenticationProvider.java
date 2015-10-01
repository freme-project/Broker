/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.broker.security;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;

import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.security.tools.PasswordHasher;
import eu.freme.common.persistence.Token;
import eu.freme.common.persistence.User;
import eu.freme.common.persistence.UserRepository;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
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
