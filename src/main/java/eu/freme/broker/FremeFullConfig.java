package eu.freme.broker;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.freme.eservices.eentity.EEntityConfig;
import eu.freme.eservices.elink.ELinkConfig;
import eu.freme.eservices.epublishing.EPublishingConfig;

/**
 * loads BrokerConfig + API endpoints
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SpringBootApplication
@ComponentScan(basePackages="eu.freme.broker.eservices")
@Import({BrokerConfig.class, EEntityConfig.class, ELinkConfig.class, EPublishingConfig.class})
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
