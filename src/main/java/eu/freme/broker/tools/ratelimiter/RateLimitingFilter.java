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
package eu.freme.broker.tools.ratelimiter;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.common.base.Strings;
import eu.freme.broker.exception.ForbiddenException;
import eu.freme.broker.exception.TooManyRequestsException;
import eu.freme.broker.tools.ExceptionHandlerService;
import eu.freme.common.persistence.tools.AccessLevelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.AbstractConfigurableEmbeddedServletContainer;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filter that limits number of requets made by each user
 *
 * @author Jonathan Sauder - jonathan.sauder@student.hpi.de
 */

@Component
public class RateLimitingFilter extends GenericFilterBean {

	@Autowired
	ExceptionHandlerService exceptionHandlerService;

	@Autowired
	RateLimiterInMemory rateLimiterInMemory;


	@Value("${ratelimiter.enabled:false}")
	boolean rateLimiterEnabled;

	public RateLimitingFilter(){
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		if (rateLimiterEnabled) {

			HttpServletRequest request = (HttpServletRequest) req;
			Authentication user = SecurityContextHolder.getContext().getAuthentication();

			String username= (user==null) ? req.getRemoteAddr() : user.getName();
			String userRole="anonymous";

			try {
				rateLimiterInMemory.addToStoredRequests(username, new Date().getTime(), req.getContentLength(), request.getRequestURI(),userRole);
			} catch (TooManyRequestsException e) {
				HttpServletResponse response = (HttpServletResponse) res;
				exceptionHandlerService.writeExceptionToResponse(request, response, e);
				return;
			}
		}

		chain.doFilter(req, res);

	}

//	public void init(FilterConfig filterConfig) {}

	public void destroy() {}


}
