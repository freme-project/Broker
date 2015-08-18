package eu.freme.broker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="")
public class InvalidNIFException extends FREMEHttpException{
	
	public InvalidNIFException(){
		
	}
	public InvalidNIFException(String msg){
		super(msg);
	}
}