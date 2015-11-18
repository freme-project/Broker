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

	private static int max_n=3;
	private static int max_t=20000;
	private int i,k;

	private ConcurrentHashMap<String,RateCounterObject> storedRequests = new ConcurrentHashMap<>();
	RateCounterObject rateCounterObject;

	public RateLimitingFilter(){
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		Authentication user = SecurityContextHolder.getContext().getAuthentication();
		String username= (user==null) ? req.getRemoteAddr() : user.getName();
		try {
			Date date=new Date();
			addToStoredRequests(username, date.getTime());
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



	//@Override
	public void addToStoredRequests(String identifier, long timestamp) throws ForbiddenException {
		try {
			rateCounterObject = storedRequests.get(identifier);
			//Array is full, now we have to compare timestamps
			if (rateCounterObject.index == max_n) {
				if (timestamp - rateCounterObject.arr[0] < max_t) {
					throw new ForbiddenException("Too many Calls - made "+max_n+" Requests in "+ (timestamp - rateCounterObject.arr[0])+" miliseconds");
				} else {

					//Find first index whose time is still worth looking at
					i = 0;
					while (i<max_n && max_t < timestamp - rateCounterObject.arr[i]) {
						i++;
					}
					//Move all elements of array left by that index, overwriting all obsolete indices
					for (k = 0; k < max_n - i; k++) {
						rateCounterObject.arr[k] = rateCounterObject.arr[k + i];
					}

					//Set the "last index" of the object correctly again
					rateCounterObject.index -= k + 1;
				}
			}
			//Array not full, the user can still make requests
			else {
				rateCounterObject.arr[rateCounterObject.index++] = timestamp;
			}
		} catch (NullPointerException e) {
			storedRequests.put(identifier, new RateCounterObject(timestamp));
		}

	}

}
