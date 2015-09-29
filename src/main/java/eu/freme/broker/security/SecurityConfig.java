/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * 					Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * 					Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import eu.freme.broker.security.database.User;
import eu.freme.broker.security.database.UserRepository;
import eu.freme.broker.security.tools.AccessLevelHelper;
import eu.freme.broker.security.tools.PasswordHasher;
import eu.freme.broker.security.voter.UserAccessDecisionVoter;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Configuration
@EnableWebMvcSecurity
@EnableScheduling
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter implements
		ApplicationContextAware {

	Logger logger = Logger.getLogger(SecurityConfig.class);

	@Autowired
	UserRepository userRepository;

	@Value("${admin.username:default}")
	private String adminUsername;

	@Value("${admin.password:default}")
	private String adminPassword;
	
	@Value("${admin.create:false}")
	private boolean createAdminUser;

	@PostConstruct
	public void init() {
		// create or promote admin user if it does not exist
		if( createAdminUser && adminUsername != null){
			createAdminUser();
		}
	}
	
	private void createAdminUser(){
		User admin = userRepository.findOneByName(adminUsername);
		if (admin == null) {
			logger.info("create new admin user");

			String saltedHashedPassword;
			try {
				saltedHashedPassword = PasswordHasher
						.getSaltedHash(adminPassword);
			} catch (Exception e) {
				logger.error(e);
				return;
			}
			admin = new User(adminUsername, saltedHashedPassword,
					User.roleAdmin);
			userRepository.save(admin);
		} else if (!admin.getRole().equals(User.roleAdmin)) {
			logger.info("promote user and change password");
			admin.setRole(User.roleAdmin);
			String saltedHashedPassword;
			try {
				saltedHashedPassword = PasswordHasher
						.getSaltedHash(adminPassword);
			} catch (Exception e) {
				logger.error(e);
				return;
			}
			admin.setPassword(saltedHashedPassword);
			userRepository.save(admin);
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.exceptionHandling()
				.authenticationEntryPoint(unauthorizedEntryPoint());

		http.addFilterBefore(new AuthenticationFilter(authenticationManager()),
				BasicAuthenticationFilter.class).addFilterBefore(
				new ManagementEndpointAuthenticationFilter(
						authenticationManager()),
				BasicAuthenticationFilter.class);
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return new AuthenticationManager() {
			@Autowired
			AuthenticationProvider[] authenticationProviders;

			@Override
			public Authentication authenticate(Authentication authentication)
					throws ProviderNotFoundException {

				for (AuthenticationProvider auth : authenticationProviders) {
					if (auth.supports(authentication.getClass())) {
						return auth.authenticate(authentication);
					}
				}

				throw new ProviderNotFoundException(
						"No AuthenticationProvider found for "
								+ authentication.getClass());
			}
		};
	}

	@Bean
	public TokenService tokenService() {
		return new TokenService();
	}

	@Bean
	public AuthenticationProvider databaseAuthenticationProvider() {
		return new DatabaseAuthenticationProvider();
	}

	@Bean
	public AuthenticationProvider tokenAuthenticationProvider() {
		return new TokenAuthenticationProvider(tokenService());
	}

	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return new AuthenticationEntryPoint() {

			@Override
			public void commence(HttpServletRequest request,
					HttpServletResponse response,
					AuthenticationException authException) throws IOException,
					ServletException {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		};
	}

	@Bean
	public AffirmativeBased defaultAccessDecisionManager() {
		@SuppressWarnings("rawtypes")
		ArrayList<AccessDecisionVoter> list = new ArrayList<AccessDecisionVoter>();
		list.add(new UserAccessDecisionVoter());
		AffirmativeBased ab = new AffirmativeBased(list);
		return ab;
	}

	@Bean
	public AccessLevelHelper accessLevelHelper() {
		return new AccessLevelHelper();
	}
}