
package eu.freme.broker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="")
public class TemplateNotFoundException extends RuntimeException {
    
	public TemplateNotFoundException(String msg){
		super(msg);
	}
}
