package eu.freme.broker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.BAD_GATEWAY, reason="External service failed")
public class ExternalServiceFailedException extends RuntimeException{

}