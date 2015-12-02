package eu.freme.broker.tools;

import eu.freme.broker.Broker;
import eu.freme.broker.exception.FREMEHttpException;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 26.10.15.
 */

@ControllerAdvice
public class BrokerExceptionHandler extends HashMap<String, String>{

    private Exception expectedException=null;
    private Logger logger;



    public BrokerExceptionHandler() {
        super();
        logger=Logger.getLogger(BrokerExceptionHandler.class);
    }

    public void expect(Exception e){
        this.expectedException=e;
    }

    public Exception getExpectedException(){
        return this.expectedException;
    }
    public void setExpectedException(Exception e) {
        expect(e);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(HttpServletRequest req, Exception exception) {
        logger.error("TESTESTESTESTEST");

        if ((this.expectedException!=null) && (exception.getClass()==expectedException.getClass())) {
            logger.info("Exception of type "+exception.getClass().toString()+" not Logged because it was expected");
        } else {
            logger.error("Request: " + req.getRequestURL() + " raised ", exception);
        }
        this.expectedException=null;


        HttpStatus statusCode = null;
        if (exception instanceof MissingServletRequestParameterException) {
            // create response for spring exceptions
            statusCode = HttpStatus.BAD_REQUEST;
        } else if (exception instanceof FREMEHttpException
                && ((FREMEHttpException) exception).getHttpStatusCode() != null) {
            // get response code from FREMEHttpException
            statusCode = ((FREMEHttpException) exception).getHttpStatusCode();
        } else if( exception instanceof AccessDeniedException){
            statusCode = HttpStatus.UNAUTHORIZED;
        } else if ( exception instanceof HttpMessageNotReadableException) {
            statusCode = HttpStatus.BAD_REQUEST;
        } else {
            // get status code from exception class annotation
            Annotation responseStatusAnnotation = exception.getClass()
                    .getAnnotation(ResponseStatus.class);
            if (responseStatusAnnotation instanceof ResponseStatus) {
                statusCode = ((ResponseStatus) responseStatusAnnotation)
                        .value();
            } else {
                // set default status code 500
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        JSONObject json = new JSONObject();
        json.put("status", statusCode.value());
        json.put("message", exception.getMessage());
        json.put("error", statusCode.getReasonPhrase());
        json.put("timestamp", new Date().getTime());
        json.put("exception", exception.getClass().getName());
        json.put("path", req.getRequestURI());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json");


        return new ResponseEntity<String>(json.toString(2), responseHeaders,
                statusCode);
    }
}
