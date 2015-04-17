package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.conversion.rdf.RDFConversionService;

@RestController
public class TildeETranslate {
	
	@Autowired
	RDFConversionService rdfConversionService;
	
	public RDFConversionService getRdfConversionService() {
		return rdfConversionService;
	}


	public void setRdfConversionService(RDFConversionService rdfConversionService) {
		this.rdfConversionService = rdfConversionService;
	}


	@RequestMapping("/e-translate/tilde")
	public ResponseEntity<String> tildeTranslate(
			@RequestParam(value = "input", defaultValue = "") String input) {
		
		if( input == null || input.trim().length() == 0 ){
			return new ResponseEntity<String>("", HttpStatus.BAD_REQUEST);
		}
		
		String response = rdfConversionService.plaintextToNifJsonLd(input);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

}
