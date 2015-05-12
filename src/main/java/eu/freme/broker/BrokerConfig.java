package eu.freme.broker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import com.github.isrsal.logging.LoggingFilter;

import eu.freme.conversion.ConversionApplicationConfig;
import eu.freme.eservices.eentity.EEntityConfig;
import eu.freme.eservices.elink.ELinkConfig;

@SpringBootApplication
@ComponentScan("eu.freme.broker.eservices")
@Import( {ConversionApplicationConfig.class, EEntityConfig.class, ELinkConfig.class})
public class BrokerConfig {
	
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