package eu.freme.broker.tools;

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
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.ReaderInputStream;
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
		contentTypes.add(EInternationalizationAPI.MIME_TYPE_HTML.toLowerCase());
		contentTypes.add(EInternationalizationAPI.MIME_TYPE_XLIFF_1_2
				.toLowerCase());
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		String informat = req.getParameter("informat");
		if (informat == null) {
			informat = req.getContentType();

			String[] parts = informat.split(";");
			if (parts.length > 1) {
				informat = parts[0].trim();
			}
		}

		if (informat == null) {
			chain.doFilter(req, res);
			return;
		}

		if (!contentTypes.contains(informat.toLowerCase())) {
			chain.doFilter(req, res);
			return;
		}

		if (!(req instanceof HttpServletRequest)) {
			chain.doFilter(req, res);
			return;
		}

		logger.debug("convert input from " + informat + " to nif");

		InputStream is = null;

		String inputQueryString = req.getParameter("input");
		if (inputQueryString == null) {
			// read data from request body
			is = req.getInputStream();
		} else {
			// read data from query string input parameter
			is = new ReaderInputStream(new StringReader("hello world"), "UTF-8");
		}

		Reader nif;
		try {
			nif = eInternationalizationApi.convertToTurtle(is,
					informat.toLowerCase());
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
