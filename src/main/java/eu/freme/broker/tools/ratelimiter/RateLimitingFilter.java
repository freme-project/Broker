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
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import eu.freme.broker.exception.TooManyRequestsException;
import eu.freme.broker.tools.ExceptionHandlerService;
import eu.freme.common.persistence.model.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.Token;

import org.springframework.security.core.userdetails.UserDetails;
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


	@Value("${ratelimiter.yaml:src/main/resources/ratelimiter.yaml}")
	String rateLimiterYaml;


	@PostConstruct
	public void setup () {
		try {
			rateLimiterInMemory.refresh(rateLimiterYaml);
		} catch (IOException e) {
			logger.error("Caugh IOException: "+rateLimiterYaml+" for YAML Configuration of Ratelimiter was not found." +
					"The Ratelimiting Filter was turned off.");
			setRateLimiterEnabled(false);
		}
	}

	Logger logger = Logger.getLogger(RateLimitingFilter.class);

	public RateLimitingFilter(){
	}

	private String username;
	private String userRole;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {



		if (rateLimiterEnabled) {

			HttpServletRequest request = (HttpServletRequest) req;
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			username=auth.getName();
			if (username.equals("anonymousUser")) {
				username=req.getRemoteAddr();
			} else {
				User user  = ((User)auth.getPrincipal());
				username=user.getName();
			}

			userRole= ((SimpleGrantedAuthority)auth.getAuthorities().toArray()[0]).getAuthority();

			long size = req.getContentLength();
			if (size==0) {
				try {
					size = request.getHeader("input").length();
				} catch (NullPointerException e){
					//Then the size is truly 0
				}
			}
			try {
				rateLimiterInMemory.addToStoredRequests(username, new Date().getTime(), size , request.getRequestURI(),userRole);
			} catch (TooManyRequestsException e) {
				HttpServletResponse response = (HttpServletResponse) res;
				exceptionHandlerService.writeExceptionToResponse(request, response, e);
				return;
			}
		}

		chain.doFilter(req, res);

	}

	/**
	 * Clears all in-Memory Timestamps & Sizes of user-made requests.
	 * Can be configured via the application.properties file
	 * Defaults to 1 hour (3 600 000 miliseconds)
	 */
	@Scheduled(fixedRateString = "${ratelimiter.clear.timer:3600000}")
	public void clearRateLimiterInMemory(){
		rateLimiterInMemory.clear();
	}

	public boolean isRateLimiterEnabled() {
		return rateLimiterEnabled;
	}
	public void setRateLimiterEnabled(boolean b){
		rateLimiterEnabled=b;
	}

	public void refresh() throws IOException {
		rateLimiterInMemory.refresh(rateLimiterYaml);
	}
	public void destroy() {}


}
