/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.broker.eservices;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import eu.freme.broker.tools.NIFParameterSet;
import eu.freme.conversion.rdf.RDFConstants;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;

/**
 * REST controller for Tilde e-Terminology service
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RestController
public class TildeETerminology extends BaseRestController {

	private String endpoint = "https://services.tilde.com/Terminology/";

	@RequestMapping(value = "/e-terminology/tilde", method = RequestMethod.POST)
	public ResponseEntity<String> tildeTranslate(
			@RequestParam(value = "input", required = false) String input,
			@RequestParam(value = "i", required = false) String i,
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
			@RequestParam(value = "outformat", required = false) String outformat,
			@RequestParam(value = "o", required = false) String o,
			@RequestParam(value = "prefix", required = false) String prefix,
			@RequestParam(value = "p", required = false) String p,
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			@RequestBody(required = false) String postBody,
			@RequestParam(value = "source-lang") String sourceLang,
			@RequestParam(value = "target-lang") String targetLang,
			@RequestParam(value = "domain", defaultValue = "") String domain) {

		// merge long and short parameters - long parameters override short
		// parameters
		if (input == null) {
			input = i;
		}
		if (informat == null) {
			informat = f;
		}
		if (outformat == null) {
			outformat = o;
		}
		if (prefix == null) {
			prefix = p;
		}
		NIFParameterSet parameters = this.normalizeNif(input, informat,
				outformat, postBody, acceptHeader, contentTypeHeader, prefix);

		// create rdf model
		String plaintext = null;
		Model inputModel = ModelFactory.createDefaultModel();

		if (!parameters.getInformat().equals(
				RDFConstants.RDFSerialization.PLAINTEXT)) {
			// input is nif
			try {
				inputModel = this.unserializeNif(parameters.getInput(),
						parameters.getInformat());
			} catch (Exception e) {
				logger.error("failed", e);
				throw new BadRequestException("Error parsing NIF input");
			}
		} else {
			// input is plaintext
			plaintext = parameters.getInput();
			rdfConversionService.plaintextToRDF(inputModel, plaintext,
					sourceLang, parameters.getPrefix());
		}

		// send request to tilde mt
		Model responseModel = null;
		try {
			String nifString = rdfConversionService.serializeRDF(inputModel,
					RDFSerialization.TURTLE);
			if( logger.isDebugEnabled() ){
				logger.debug( "send nif to tilde e-terminology: \n" + nifString);
			}
			HttpResponse<String> response = Unirest.post(endpoint)
					.queryString("sourceLang", sourceLang)
					.queryString("targetLang", targetLang)
					.queryString("domain", domain)
					.header("Accept", "application/turtle")
					.header("Content-Type", "application/turtle")
					.body(nifString).asString();

			if (response.getStatus() != HttpStatus.OK.value()) {
				throw new ExternalServiceFailedException(
						"External service failed with status code "
								+ response.getStatus(),
						HttpStatus.valueOf(response.getStatus()));
			}

			String translation = response.getBody();

			responseModel = rdfConversionService.unserializeRDF(translation,
					RDFSerialization.TURTLE);

		} catch (Exception e) {
			if (e instanceof ExternalServiceFailedException) {
				throw new ExternalServiceFailedException(e.getMessage(),
						((ExternalServiceFailedException) e)
								.getHttpStatusCode());
			} else {
				throw new ExternalServiceFailedException(e.getMessage());
			}
		}
		
		return createSuccessResponse(responseModel, parameters.getOutformat());
	}
}
