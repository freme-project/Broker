package eu.freme.broker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.isrsal.logging.LoggingFilter;

import eu.freme.broker.tools.NIFParameterFactory;
import eu.freme.broker.tools.RDFSerializationFormats;

@Configuration
public class FremeCommonConfig {
	
    @Bean
    public RDFSerializationFormats rdfFormats(){
    	return new RDFSerializationFormats();
    }
    @Bean
    public NIFParameterFactory getNifParameterFactory(){
    	return new NIFParameterFactory();
    }
    

	/**
	 * Create a filter that logs all requests input and output
	 */
    @Bean
    public FilterRegistrationBean loggingFilter() {
    	FilterRegistrationBean filter = new FilterRegistrationBean();
    	filter.setFilter(new LoggingFilter());
        List<String> urlPatterns = new ArrayList<String>();
        urlPatterns.add("/*");
    	filter.setUrlPatterns(urlPatterns);
        return filter;
    }
}
