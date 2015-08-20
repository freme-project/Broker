package eu.freme.broker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Gerald Haesendonck
 */
@SuppressWarnings("serial")
@ResponseStatus(value= HttpStatus.NOT_ACCEPTABLE, reason="Invalid input")
public class NotAcceptableException extends FREMEHttpException {
	public NotAcceptableException(String msg) {
		super(msg);
	}
}
