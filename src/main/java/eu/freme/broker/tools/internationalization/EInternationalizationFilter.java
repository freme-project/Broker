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
package eu.freme.broker.tools.internationalization;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.i18n.api.EInternationalizationAPI;
import eu.freme.i18n.okapi.nif.converter.ConversionException;

/**
 * Filter that converts HTML and XLIFF input to NIF and changes the informat
 * parameter. It also performs roundtripping, e.g. to convert HTML to NIF and
 * then back to HTML.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */

@Component
@Profile("broker")
public class EInternationalizationFilter implements Filter {

	/*
	 * EInternationalization accepts these formats for conversion to NIF
	 */
	private HashSet<String> contentTypes;

	/*
	 * EInternationalization accepts these formats for roundtripping
	 */
	private HashSet<String> outputFormats;

	private Logger logger = Logger.getLogger(EInternationalizationFilter.class);

	@Autowired
	EInternationalizationAPI eInternationalizationApi;

	public EInternationalizationFilter() {
		contentTypes = new HashSet<String>();
		contentTypes.add(EInternationalizationAPI.MIME_TYPE_HTML.toLowerCase());
		contentTypes.add(EInternationalizationAPI.MIME_TYPE_XLIFF_1_2
				.toLowerCase());
		contentTypes.add(EInternationalizationAPI.MIME_TYPE_XML.toLowerCase());
		contentTypes.add(EInternationalizationAPI.MIME_TYPE_ODT.toLowerCase());

		outputFormats = new HashSet<String>();
		outputFormats
				.add(EInternationalizationAPI.MIME_TYPE_HTML.toLowerCase());
		outputFormats.add(EInternationalizationAPI.MIME_TYPE_XLIFF_1_2
				.toLowerCase());
	}

	/**
	 * Determines format of request. Returns null if the format is not suitable
	 * for eInternationalization Filter.
	 * 
	 * @param req
	 * @return
	 */
	public String getInformat(HttpServletRequest req) {
		String informat = req.getParameter("informat");
		if (informat == null && req.getContentType() != null) {
			informat = req.getContentType();
			String[] parts = informat.split(";");
			if (parts.length > 1) {
				informat = parts[0].trim();
			}
		}
		if (informat == null) {
			return informat;
		}
		informat = informat.toLowerCase();
		if (contentTypes.contains(informat)) {
			return informat;
		} else {
			return null;
		}
	}

	/**
	 * Determines format of request. Returns null if the format is not suitable
	 * for eInternationalization Filter.
	 * 
	 * @param req
	 * @return
	 */
	public String getOutformat(HttpServletRequest req) {
		String outformat = req.getParameter("outformat");
		if (outformat == null && req.getHeader("Accept") != null) {
			outformat = req.getHeader("Accept");
			String[] parts = outformat.split(";");
			if (parts.length > 1) {
				outformat = parts[0].trim();
			}
		}
		if (outformat == null) {
			return null;
		}
		outformat = outformat.toLowerCase();
		if (contentTypes.contains(outformat)) {
			return outformat;
		} else {
			return null;
		}
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		if (!(req instanceof HttpServletRequest)
				|| !(res instanceof HttpServletResponse)) {
			chain.doFilter(req, res);
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) req;
		HttpServletResponse httpResponse = (HttpServletResponse) res;

		String informat = getInformat(httpRequest);
		String outformat = getOutformat(httpRequest);

		if (outformat != null
				&& (informat == null || !outformat.equals(informat))) {
			throw new BadRequestException("Can only convert to outformat \""
					+ outformat + "\" when informat is also \"" + outformat
					+ "\"");
		}

		if (outformat != null && !outputFormats.contains(outformat)) {
			throw new BadRequestException("\"" + outformat
					+ "\" is not a valid output format");
		}

		if (informat == null) {
			chain.doFilter(req, res);
			return;
		}

		boolean roundtripping = false;
		if (outformat != null) {
			roundtripping = true;
			logger.debug("convert from " + informat + " to " + outformat);
		} else {
			logger.debug("convert input from " + informat + " to nif");
		}

		// do conversion of informat to nif
		// create BodySwappingServletRequest

		InputStream requestInputStream = null;

		String inputQueryString = req.getParameter("input");
		if (inputQueryString == null) {
			// read data from request body
			requestInputStream = req.getInputStream();
		} else {
			// read data from query string input parameter
			requestInputStream = new ReaderInputStream(new StringReader(
					inputQueryString), "UTF-8");
		}

		// copy request content to buffer
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bis = new BufferedInputStream(requestInputStream);
		byte[] buffer = new byte[1024];
		int read = 0;
		while ((read = bis.read(buffer)) != -1) {
			baos.write(buffer, 0, read);
		}
		bis.close();

		// create request wrapper that converts the body of the request from the
		// original format to turtle
		Reader nif;

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		try {
			nif = eInternationalizationApi.convertToTurtle(bais,
					informat.toLowerCase());
		} catch (ConversionException e) {
			logger.error("Error", e);
			throw new InternalServerErrorException();
		}
		BodySwappingServletRequest bssr = new BodySwappingServletRequest(
				(HttpServletRequest) req, nif, roundtripping);

		// do conversion from turtle to original format if needed
		ServletResponse newResponse = res;
		if (roundtripping) {
			InputStream originalInputStream = new ByteArrayInputStream(
					baos.toByteArray());
			try {
				newResponse = new ConversionHttpServletResponseWrapper(
						httpResponse, eInternationalizationApi,
						originalInputStream, informat, outformat);
			} catch (ConversionException e) {
				logger.error("Conversion failed", e);
				throw new InternalServerErrorException("Conversion failed");
			}
		}

		chain.doFilter(bssr, newResponse);
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}
}
