package eu.freme.broker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrrorException extends RuntimeException{
	public InternalServerErrrorException(String msg){
		super(msg);
	}
}
