/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.UrlPathHelper;

import com.google.common.base.Optional;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class ManagementEndpointAuthenticationFilter extends GenericFilterBean {

	private final static Logger logger = LoggerFactory
			.getLogger(ManagementEndpointAuthenticationFilter.class);
	private AuthenticationManager authenticationManager;
	private Set<String> managementEndpoints;

	public ManagementEndpointAuthenticationFilter(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
		managementEndpoints = new HashSet<>();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = asHttp(request);
		HttpServletResponse httpResponse = asHttp(response);

		Optional<String> username = Optional.fromNullable(httpRequest
				.getHeader("X-Auth-Username"));
		Optional<String> password = Optional.fromNullable(httpRequest
				.getHeader("X-Auth-Password"));

		String resourcePath = new UrlPathHelper()
				.getPathWithinApplication(httpRequest);

		try {
			if (postToManagementEndpoints(resourcePath)) {
				logger.debug(
						"Trying to authenticate user {} for management endpoint by X-Auth-Username method",
						username);
				processManagementEndpointUsernamePasswordAuthentication(
						username, password);
			}

			logger.debug("ManagementEndpointAuthenticationFilter is passing request down the filter chain");
			chain.doFilter(request, response);
		} catch (AuthenticationException authenticationException) {
			SecurityContextHolder.clearContext();
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					authenticationException.getMessage());
		}
	}

	private HttpServletRequest asHttp(ServletRequest request) {
		return (HttpServletRequest) request;
	}

	private HttpServletResponse asHttp(ServletResponse response) {
		return (HttpServletResponse) response;
	}

	private boolean postToManagementEndpoints(String resourcePath) {
		return managementEndpoints.contains(resourcePath);
	}

	private void processManagementEndpointUsernamePasswordAuthentication(
			Optional<String> username, Optional<String> password)
			throws IOException {
		Authentication resultOfAuthentication = tryToAuthenticateWithUsernameAndPassword(
				username, password);
		SecurityContextHolder.getContext().setAuthentication(
				resultOfAuthentication);
	}

	private Authentication tryToAuthenticateWithUsernameAndPassword(
			Optional<String> username, Optional<String> password) {
		UsernamePasswordAuthenticationToken requestAuthentication = new UsernamePasswordAuthenticationToken (
				username, password);
		return tryToAuthenticate(requestAuthentication);
	}

	private Authentication tryToAuthenticate(
			Authentication requestAuthentication) {
		Authentication responseAuthentication = authenticationManager
				.authenticate(requestAuthentication);
		if (responseAuthentication == null
				|| !responseAuthentication.isAuthenticated()) {
			throw new InternalAuthenticationServiceException(
					"Unable to authenticate Backend Admin for provided credentials");
		}
		logger.debug("Backend Admin successfully authenticated");
		return responseAuthentication;
	}
}
