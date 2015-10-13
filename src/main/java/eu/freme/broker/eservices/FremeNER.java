package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.fremener.FremeNer;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RestController
@Profile("fremener")
public class FremeNER{
	
	@Autowired
	FremeNer fremeNer;
	
    @RequestMapping(value = "/e-entity/freme-ner/documents", method = RequestMethod.POST)
	public ResponseEntity<String> test(
		@RequestBody String text,
		@RequestParam String language,
		@RequestParam String outformat,
		@RequestParam String rdfPrefix
	){
		String response = fremeNer.spot(text, language, outformat, rdfPrefix);
		return new ResponseEntity<String>(response, HttpStatus.OK);
    }
}
