package eu.freme.broker.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SuppressWarnings("serial")
public class FREMEHttpException extends RuntimeException{

	HttpStatus httpStatusCode;
	
	public FREMEHttpException(){
		super();
	}
	
	public FREMEHttpException(String msg){
		super(msg);
	}

	public FREMEHttpException(String msg, HttpStatus httpStatusCode){
		super(msg);
		setHttpStatusCode(httpStatusCode);
	}

	public FREMEHttpException(HttpStatus httpStatusCode){
		super();
		setHttpStatusCode(httpStatusCode);
	}

	public HttpStatus getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(HttpStatus httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
}

