package eu.freme.broker.eservices;

import java.lang.annotation.Annotation;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hp.hpl.jena.rdf.model.Model;

import eu.freme.broker.exception.FREMEHttpException;
import eu.freme.broker.tools.NIFParameterFactory;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.broker.tools.RDFELinkSerializationFormats;
import eu.freme.broker.tools.RDFSerializationFormats;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConversionService;

/**
 * Common codes for all rest controllers.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public abstract class BaseRestController {

	Logger logger = Logger.getLogger(BaseRestController.class);

	@Autowired
	RDFConversionService rdfConversionService;

	@Autowired
	NIFParameterFactory nifParameterFactory;

	@Autowired
	RDFSerializationFormats rdfSerializationFormats;

	@Autowired
	RDFELinkSerializationFormats rdfELinkSerializationFormats;

	/**
	 * Create a NIFParameterSet to make dealing with NIF API specifications
	 * easier. It handles informat overwrites Content-Type header, input
	 * overwrites post body, and more.
	 * 
	 * @param input
	 * @param informat
	 * @param outformat
	 * @param postBody
	 * @param acceptHeader
	 * @param contentTypeHeader
	 * @param prefix
	 * @return
	 */
	protected NIFParameterSet normalizeNif(String input, String informat,
			String outformat, String postBody, String acceptHeader,
			String contentTypeHeader, String prefix) {
		return nifParameterFactory.constructFromHttp(input, informat,
				outformat, postBody, acceptHeader, contentTypeHeader, prefix);
	}

	/**
	 * Convert Jena model to string.
	 * 
	 * @param model
	 * @param format
	 * @return
	 * @throws Exception
	 */
	protected String serializeNif(Model model,
			RDFConstants.RDFSerialization format) throws Exception {
		return rdfConversionService.serializeRDF(model, format);
	}

	/**
	 * Convert string to Jena model.
	 * 
	 * @param nif
	 * @param format
	 * @return
	 * @throws Exception
	 */
	protected Model unserializeNif(String nif,
			RDFConstants.RDFSerialization format) throws Exception {
		return rdfConversionService.unserializeRDF(nif, format);
	}

	/**
	 * Custom exception handler for all FREME endpoints. All exceptions (except
	 * security exceptions) get here.
	 * 
	 * @param req
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleError(HttpServletRequest req,
			Exception exception) {
		logger.error("Request: " + req.getRequestURL() + " raised ", exception);

		HttpStatus statusCode = null;
		if (exception instanceof MissingServletRequestParameterException) {
			// create response for spring exceptions
			statusCode = HttpStatus.BAD_REQUEST;
		} else if (exception instanceof FREMEHttpException
				&& ((FREMEHttpException) exception).getHttpStatusCode() != null) {
			// get response code from FREMEHttpException
			statusCode = ((FREMEHttpException) exception).getHttpStatusCode();
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

	/**
	 * Create a ResponseEntity for a REST API method. It accepts a Jena Model
	 * and an RDFSerialization format. It converts the model to a string in the
	 * desired serialization format and sets the right Content-Type header.
	 * 
	 * @param rdf
	 * @param rdfFormat
	 * @return
	 */
	public ResponseEntity<String> createSuccessResponse(Model rdf,
			RDFConstants.RDFSerialization rdfFormat) {
		HttpHeaders responseHeaders = new HttpHeaders();
		String contentTypeHeader = rdfSerializationFormats
				.getContentTypeHeader(rdfFormat);
		responseHeaders.add("Content-Type", contentTypeHeader);
		String rdfString;
		try {
			rdfString = serializeNif(rdf, rdfFormat);
		} catch (Exception e) {
			throw new InternalServerErrorException();
		}
		return new ResponseEntity<>(rdfString, responseHeaders, HttpStatus.OK);
	}

}
