package eu.freme.broker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="")
public class InternalServerErrorException extends FREMEHttpException{
	public InternalServerErrorException(String msg){
		super(msg);
	}
}