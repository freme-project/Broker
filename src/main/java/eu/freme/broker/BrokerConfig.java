package eu.freme.broker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.github.isrsal.logging.LoggingFilter;

import eu.freme.broker.tools.CORSFilter;
import eu.freme.broker.tools.NIFParameterFactory;
import eu.freme.broker.tools.RDFELinkSerializationFormats;
import eu.freme.broker.tools.RDFSerializationFormats;
import eu.freme.conversion.ConversionApplicationConfig;

/**
 * configures broker without api endpoints and e-Services
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */

@SpringBootApplication
@Import(ConversionApplicationConfig.class)
public class BrokerConfig {
	
	/**
	 * Create a filter that logs all requests input and output
	 * @return
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
    
    @Bean
    public RDFSerializationFormats rdfFormats(){
    	return new RDFSerializationFormats();
    }
    
    @Bean
    public RDFELinkSerializationFormats eLinkRdfFormats(){
    	return new RDFELinkSerializationFormats();
    }

    @Bean
    public NIFParameterFactory getNifParameterFactory(){
    	return new NIFParameterFactory();
    }

}