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
package eu.freme.broker.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.i18n.api.EInternationalizationAPI;
import eu.freme.i18n.okapi.nif.converter.ConversionException;

/**
 * Filter that converts HTML and XLIFF input to NIF and changes the informat
 * parameter.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */

@Component
public class EInternationalizationFilter implements Filter {

	private HashSet<String> contentTypes;
	private Logger logger = Logger.getLogger(EInternationalizationFilter.class);
	
	@Autowired
	EInternationalizationAPI eInternationalizationApi;

	public EInternationalizationFilter() {
		contentTypes = new HashSet<String>();
		contentTypes.add(EInternationalizationAPI.MIME_TYPE_HTML);
		contentTypes.add(EInternationalizationAPI.MIME_TYPE_XLIFF_1_2);
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		String informat = req.getParameter("informat");
		if( informat == null ){
			informat = req.getContentType();
		}
		
		if( informat == null ){
			chain.doFilter(req, res);
			return;
		}
		
		if( !contentTypes.contains(informat.toLowerCase())){
			chain.doFilter(req, res);
			return;
		}
		
		if (!(req instanceof HttpServletRequest)) {
			chain.doFilter(req, res);
			return;
		}
		
		logger.debug( "convert input from " + informat + " to nif");

		ServletInputStream is = req.getInputStream();
		Reader nif;
		try {
			nif = eInternationalizationApi.convertToTurtle(is, informat.toLowerCase());
		} catch (ConversionException e) {
			logger.error("Error", e);
			throw new InternalServerErrorException();
		}
		
		BodySwappingServletRequest bssr = new BodySwappingServletRequest(
				(HttpServletRequest) req, nif);
		chain.doFilter(bssr, res);
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}

}
