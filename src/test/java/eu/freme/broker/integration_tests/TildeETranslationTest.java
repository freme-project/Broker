package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;

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
		return baseRequest("")
				.queryString("source-lang", sourceLang)
				.queryString("target-lang", targetLang);
	}

	@Test
	public void testEtranslate() throws UnirestException, IOException, Exception {

		HttpResponse<String> response = baseRequest()
				.queryString("input", "hello world")
				.queryString("informat", "text")
				.queryString("outformat","rdf-xml")
				.asString();
		assertTrue(response.getStatus() == 200);

		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.RDF_XML));
		// validate NIF
		Validate.main(new String[]{"-i", response.getBody(), "--informat", "rdfxml"});

		String data = readFile("src/test/resources/rdftest/e-translate/data.turtle");
		response = baseRequest().header("Content-Type", "text/turtle")
				.body(data).asString();

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.TURTLE));
		// validate NIF
		Validate.main(new String[]{"-i", response.getBody(),"--informat","turtle"});

		data = readFile("src/test/resources/rdftest/e-translate/data.json");
		response = baseRequest().header("Content-Type", "application/json+ld")
				.queryString("outformat","json-ld")
				.body(data).asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.JSON_LD));
		// validate NIF
		//// Validator: informat=json-ld not implemented yet
		//Validate.main(new String[]{"-i", response.getBody(), "--informat", "json-ld"});
		
		data = readFile("src/test/resources/rdftest/e-translate/data.txt");
		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text")
				.queryString("outformat","n3")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.N3));
		// validate NIF
		//// Validator: informat=n3 not implemented yet
		//Validate.main(new String[]{"-i", response.getBody(), "--informat","n3"});


		response = baseRequest()
				.queryString("input", URLEncoder.encode(data, "UTF-8"))
				.queryString("informat", "text")
				.queryString("outformat","n-triples")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		assertNotNull(converter.unserializeRDF(response.getBody(), RDFConstants.RDFSerialization.N_TRIPLES));
		// validate NIF
		//// Validator: informat=ntriples not implemented yet
		//Validate.main(new String[]{"-i", response.getBody(), "--informat","ntriples"});
	}
}
