package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;

import com.hp.hpl.jena.vocabulary.RDF;
import org.junit.Ignore;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class TildeETranslationTest extends IntegrationTest{


	String clientId = "u-bd13faca-b816-4085-95d5-05373d695ab7";
	String sourceLang = "en";
	String targetLang = "de";
	String translationSystemId = "smt-76cd2e73-05c6-4d51-b02f-4fc9c4d40813";
	

	public TildeETranslationTest(){super("/e-translation/tilde");}

	private HttpRequestWithBody baseRequest() {
		return baseRequestPost("").queryString("client-id", clientId)
				.queryString("source-lang", sourceLang)
				.queryString("target-lang", targetLang)
				.queryString("translation-system-id", translationSystemId);
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

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);

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
