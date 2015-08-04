/**
 * Copyright (C) 2015 Felix Sasaki (Felix.Sasaki@dfki.de)
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
package eu.freme.broker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.github.isrsal.logging.LoggingFilter;

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