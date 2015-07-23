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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

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

	public EInternationalizationFilter() {
		contentTypes = new HashSet<String>();
		contentTypes.add("text/html");
		contentTypes.add("application/x-xliff+xml");
		contentTypes.add("application/xliff+xml");
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

		// TODO: create nif here from req.getReader() or
		// req.getInputStream()

		String nif = "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n"
				+ "@prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n"
				+ "<http://example.org/document/1#char=0,19>\n"
				+ " a nif:String , nif:Context , nif:RFC5147String ;\n"
				+ " nif:isString \"Welcome to e-Internationalization\"@en;\n"
				+ " nif:beginIndex \"0\"^^xsd:nonNegativeInteger;\n"
				+ " nif:endIndex \"19\"^^xsd:nonNegativeInteger;\n"
				+ " nif:sourceUrl <http://differentday.blogspot.com/2007_01_01_archive.html> .";

		StringReader reader = new StringReader(nif);

		BodySwappingServletRequest bssr = new BodySwappingServletRequest(
				(HttpServletRequest) req, reader);
		chain.doFilter(bssr, res);
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}

}
