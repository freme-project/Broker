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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
@Profile("broker")
public class EInternationalizationOutputFilter implements Filter {

	private Logger logger = Logger.getLogger(EInternationalizationOutputFilter.class);

	@Autowired
	EInternationalizationAPI eInternationalizationApi;

	public EInternationalizationOutputFilter() {
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
//
//		String informat = req.getParameter("informat");
//		if (informat == null && req.getContentType() != null) {
//			informat = req.getContentType();
//			String[] parts = informat.split(";");
//			if (parts.length > 1) {
//				informat = parts[0].trim();
//			}
//		}

		res.getOutputStream().write(new String("xxxxxxxxxxx").getBytes());

		chain.doFilter(req, res);
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}

	/**
	 * debug function to see string content of inputstream. not used in
	 * production. take care: the inputstream is read and cannot be read again
	 * when it has ran through this function.
	 * 
	 * @param is
	 * @return
	 */
	@SuppressWarnings("unused")
	private String convertInputStreamToString(InputStream is) {
		final char[] buffer = new char[1024];
		final StringBuilder out = new StringBuilder();
		try (Reader in = new InputStreamReader(is, "UTF-8")) {
			for (;;) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}

		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return out.toString();
	}
}
