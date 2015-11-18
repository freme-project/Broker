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
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.freme.broker.exception.ForbiddenException;
import eu.freme.broker.tools.ExceptionHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.AbstractConfigurableEmbeddedServletContainer;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Filter that limits number of requets made by each user
 *
 * @author Jonathan Sauder - jonathan.sauder@student.hpi.de
 */

@Component
public class RateLimitingFilter implements Filter {

	@Autowired
	ExceptionHandlerService exceptionHandlerService;

	@Autowired
	RateLimiterInMemory rateLimiterInMemory;

	public RateLimitingFilter(){
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		//TODO:
		rateLimiterInMemory.setup();


		Authentication user = SecurityContextHolder.getContext().getAuthentication();
		String username= (user==null) ? req.getRemoteAddr() : user.getName();
		try {
			Date date=new Date();
			rateLimiterInMemory.addToStoredRequests(username, date.getTime());
		} catch (ForbiddenException e){
			HttpServletResponse response=(HttpServletResponse)res;
			HttpServletRequest request=(HttpServletRequest)req;
			exceptionHandlerService.writeExceptionToResponse(request, response, e);
			return;
		}
		chain.doFilter(req, res);
	}

	public void init(FilterConfig filterConfig) {}

	public void destroy() {}


}
