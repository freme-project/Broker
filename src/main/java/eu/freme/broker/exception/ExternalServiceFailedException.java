package eu.freme.broker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.BAD_GATEWAY)
public class ExternalServiceFailedException extends FREMEHttpException{

	public ExternalServiceFailedException(){
		
	}
	
	public ExternalServiceFailedException(String msg){
		super(msg);
	}
	
	public ExternalServiceFailedException(String msg, HttpStatus httpStatusCode){
		super(msg, httpStatusCode);
	}
}
