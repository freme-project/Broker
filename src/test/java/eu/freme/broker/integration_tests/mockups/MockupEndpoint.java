package eu.freme.broker.integration_tests.mockups;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

import eu.freme.broker.eservices.BaseRestController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MockupEndpoint extends BaseRestController {

	// use regEx to include the file extension
	@RequestMapping("/mockups/file/{filename:.+}")
	public ResponseEntity<String> sendRDFfileContent(
			@RequestHeader( value="outformat", required=false) String outformat,
			@RequestHeader( value="Content-Type", required=false) String contentType,
			@PathVariable String filename

	) throws IOException{

		File file = new File("src/test/resources/mockup-endpoint-data/"+filename);
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
