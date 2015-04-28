package eu.freme.broker.eservices;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BaseRestController {

	/**
	 * Create this response in the REST controller when the external service is unavailable or has reported an error.
	 * 
	 * @return
	 */
	public ResponseEntity<String> externalServiceFailedResponse(){
		return new ResponseEntity<String>("The external service failed", HttpStatus.BAD_GATEWAY);
	}
}
