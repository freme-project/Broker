//package eu.freme.broker.tools.internationalization;
//
//import java.io.IOException;
//import java.util.HashSet;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpServletResponseWrapper;
//
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//import eu.freme.i18n.api.EInternationalizationAPI;
//
//@Component
//@Profile("broker")
//public class EInternationalizationFilter implements Filter {
//
//	private HashSet<String> contentTypes;
//	private Logger logger = Logger.getLogger(EInternationalizationFilter.class);
//
//	@Autowired
//	EInternationalizationAPI eInternationalizationApi;
//
//	public EInternationalizationFilter() {
//		contentTypes = new HashSet<String>();
//		contentTypes.add(EInternationalizationAPI.MIME_TYPE_HTML.toLowerCase());
//		contentTypes.add(EInternationalizationAPI.MIME_TYPE_XLIFF_1_2
//				.toLowerCase());
//	}
//
//	public void doFilter(ServletRequest req, ServletResponse res,
//			FilterChain chain) throws IOException, ServletException {
//		
//		System.err.println("aaa");
//		if( !(res instanceof HttpServletResponse )){
//			chain.doFilter(req, res);
//			return;
//		}
//		
//		System.err.println("xxx");
//		HttpServletResponse httpResponse = (HttpServletResponse)res;
//		chain.doFilter(req, new TestWrapper(httpResponse));
//	}
//
//	@Override
//	public void init(FilterConfig filterConfig) throws ServletException {
//	}
//
//	@Override
//	public void destroy() {
//	}
//}
