package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;

import com.hp.hpl.jena.vocabulary.RDF;
import org.junit.Ignore;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.conversion.rdf.*;
import org.nlp2rdf.cli.Validate;


public class TildeETranslationTest extends IntegrationTest{

	//String url = null;

	String sourceLang = "en";
	String targetLang = "de";

	public TildeETranslationTest(){super("/e-translation/tilde");}

	private HttpRequestWithBody baseRequest() {
		return baseRequestPost("")
				.queryString("source-lang", sourceLang)
				.queryString("target-lang", targetLang);
	}

	@Test
	@Ignore //TODO: wait for Issue: Timeouts on e-Terminology https://github.com/freme-project/Broker/issues/43
	public void testEtranslate() throws UnirestException, IOException, Exception {

		HttpResponse<String> response = baseRequest()
				.queryString("input", "hello world")
				.queryString("informat", "text")
				.queryString("outformat","rdf-xml")
				.asString();
		validateNIFResponse(response, RDFConstants.RDFSerialization.RDF_XML);


		String data = readFile("src/test/resources/rdftest/e-translate/data.turtle");
		response = baseRequest().header("Content-Type", "text/turtle")
				.body(data).asString();
		validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);


		data = readFile("src/test/resources/rdftest/e-translate/data.json");
		response = baseRequest().header("Content-Type", "application/json+ld")
				.queryString("outformat","json-ld")
				.body(data).asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		validateNIFResponse(response, RDFConstants.RDFSerialization.JSON_LD);
		
		data = readFile("src/test/resources/rdftest/e-translate/data.txt");
		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text")
				.queryString("outformat","n3")
				.asString();
		validateNIFResponse(response, RDFConstants.RDFSerialization.N3);


		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text")
				.queryString("outformat","n-triples")
				.asString();
		validateNIFResponse(response, RDFConstants.RDFSerialization.N_TRIPLES);
	}
}
