package eu.freme.broker.integration_tests.mockups;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ELinkMockupEndpoint {
	
	@RequestMapping("mockups/sparql")
	public ResponseEntity<String> sendRDF(
			@RequestHeader( value="outformat") String outformat,
			@RequestHeader( value="Content-Type") String contentType
	) throws IOException{


		File file = new File("src/test/resources/mockup-endpoint-data/dbpedia-spotlight.txt");
		String fileContent = FileUtils.readFileToString(file);		
		HttpHeaders headers = new HttpHeaders();

		contentType= (contentType == null) ? "text/turtle" : contentType;

		headers.add("Content-Type", contentType);
		outformat= (outformat == null) ? "turtle" : outformat;
		headers.add("outformat",outformat);


		ResponseEntity<String> response = new ResponseEntity<String>(fileContent, headers, HttpStatus.OK);
		return response;
	}
}