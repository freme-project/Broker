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
