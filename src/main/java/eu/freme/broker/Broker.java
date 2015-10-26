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

import java.io.File;

import javax.annotation.PostConstruct;

import eu.freme.broker.tools.BrokerExceptionHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.freme.broker.tools.RDFELinkSerializationFormats;
import eu.freme.broker.tools.StarterHelper;
import eu.freme.common.FREMECommonConfig;
import eu.freme.eservices.eentity.EEntityConfig;
import eu.freme.eservices.elink.ELinkConfig;
import eu.freme.eservices.epublishing.EPublishingConfig;
import eu.freme.eservices.pipelines.api.PipelineConfig;
import eu.freme.i18n.api.EInternationalizationConfig;

/**
 * configures broker without api endpoints and e-Services
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */

@SpringBootApplication
@Import({ FremeCommonConfig.class, EEntityConfig.class, ELinkConfig.class,
		EPublishingConfig.class, FREMECommonConfig.class,
		PipelineConfig.class, EInternationalizationConfig.class })
@Profile("broker")
public class Broker {
	
	static Logger logger = Logger.getLogger(Broker.class);

	@Value("${workspace.location}")
	String workspaceLocation;

    @Bean
    public RDFELinkSerializationFormats eLinkRdfFormats(){
    	return new RDFELinkSerializationFormats();
    }

	@Bean
	public BrokerExceptionHandler brokerExceptionHandler() { return new BrokerExceptionHandler(); }


	@PostConstruct
	public void init() {
		// create workspace folder
		File workspace = new File(workspaceLocation);
		if (!workspace.exists()) {
			workspace.mkdirs();
		}
	}

	public void setWorkspaceLocation(String workspaceLocation) {
		this.workspaceLocation = workspaceLocation;
	}
	
	public static void main(String[] args){
		logger.info("Starting FREME in Broker mode");
		String[] newArgs = StarterHelper.addProfile(args, "broker");
		SpringApplication.run(Broker.class, newArgs);
	}
}