package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.ExternalServiceFailedException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.common.conversion.etranslate.TranslationConversionService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RestController
public class SecurityController extends BaseRestController {

	@RequestMapping(value = "/security", method = RequestMethod.POST)
	public ResponseEntity<String> tildeTranslate() {

		return new ResponseEntity<String>("hello", HttpStatus.OK);
	}
}
