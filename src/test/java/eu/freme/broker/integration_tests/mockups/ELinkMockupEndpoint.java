package eu.freme.broker.integration_tests.mockups;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ELinkMockupEndpoint {
	
	@RequestMapping("mockups/sparql")
	public ResponseEntity<String> sendRDF() throws IOException{
		File file = new File("src/test/resources/mockup-endpoint-data/dbpedia-spotlight.txt");
		String fileContent = FileUtils.readFileToString(file);		
		ResponseEntity<String> response = new ResponseEntity<String>(fileContent, HttpStatus.OK);
		response.getHeaders().add("Content-Type", "text/turtle");
		return response;
	}
}
