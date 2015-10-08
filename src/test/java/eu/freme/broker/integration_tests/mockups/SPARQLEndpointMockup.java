package eu.freme.broker.integration_tests.mockups;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SPARQLEndpointMockup {
	
	@RequestMapping("mockups/sparql")
	public String sendRDF() throws IOException{
		File file = new File("src/test/resources/mockup-endpoint-data/dbpedia-spotlight.txt");
		return "xxx";
//		return FileUtils.readFileToString(file);
	}
}
