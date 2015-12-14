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
package eu.freme.broker;

import java.util.ArrayList;
import java.util.List;

import eu.freme.broker.tools.ratelimiter.RateLimiterInMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import eu.freme.broker.tools.loggingfilter.LoggingFilter;

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

    @Bean
    public RateLimiterInMemory getRateLimiterInMemory() {
        return new RateLimiterInMemory();
    }

	/**
	 * Create a filter that logs all requests input and output
	 *//*
    @Bean
    public FilterRegistrationBean loggingFilter() {
    	FilterRegistrationBean filter = new FilterRegistrationBean();
    	filter.setFilter(new LoggingFilter());
        List<String> urlPatterns = new ArrayList<String>();
        urlPatterns.add("/*");
    	filter.setUrlPatterns(urlPatterns);
        filter.setOrder(0);
        return filter;
    }*/
}
