/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.freme.conversion.ConversionApplicationConfig;
import eu.freme.eservices.eentity.EEntityConfig;
import eu.freme.eservices.elink.ELinkConfig;
import eu.freme.eservices.epublishing.EPublishingConfig;
import eu.freme.eservices.pipelines.api.PipelineConfig;
import eu.freme.i18n.api.EInternationalizationConfig;

/**
 * loads BrokerConfig and API endpoints
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SpringBootApplication
@ComponentScan(basePackages = "eu.freme.broker.eservices")
@Import({ BrokerConfig.class, EEntityConfig.class, ELinkConfig.class,
		EPublishingConfig.class, ConversionApplicationConfig.class,
		PipelineConfig.class, EInternationalizationConfig.class })
public class FremeFullConfig {

	@Value("${workspace.location}")
	String workspaceLocation;

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
	

}
