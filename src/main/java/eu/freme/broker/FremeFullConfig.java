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

/**
 * loads BrokerConfig and API endpoints
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SpringBootApplication
@ComponentScan(basePackages="eu.freme.broker.eservices")
@Import({BrokerConfig.class, EEntityConfig.class, ELinkConfig.class, EPublishingConfig.class, ConversionApplicationConfig.class, PipelineConfig.class})
public class FremeFullConfig {

	@Value("${workspace.location}")
	String workspaceLocation;
	
	@PostConstruct
	public void init() {
		// create workspace folder
		File workspace = new File(workspaceLocation);
		if( !workspace.exists() ){
			workspace.mkdirs();
		}
	}

	public void setWorkspaceLocation(String workspaceLocation) {
		this.workspaceLocation = workspaceLocation;
	}
}
