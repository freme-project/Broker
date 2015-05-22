package eu.freme.broker.tools;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.freme.broker.BrokerConfig;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class NIFParameterFactoryTest {

	@Autowired
	NIFParameterFactory nifParameterFactory;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testConstructFromHttp() {

		// test input parameter
		NIFParameterSet set = nifParameterFactory.constructFromHttp("input", "text/turtle",
				"text/turtle", "abc", "text/turtle", "text/turtle",
				"http://example.org");
		assertTrue(set.getInput().equals("input"));

		set = nifParameterFactory.constructFromHttp(null, "text/turtle",
				"text/turtle", "input", "text/turtle", "text/turtle",
				"http://example.org");
		assertTrue(set.getInput().equals("input"));
		
		thrown.expect(BadRequestException.class);
		nifParameterFactory.constructFromHttp(null, "text/turtle",
				"text/turtle", null, "text/turtle", "text/turtle",
				"http://example.org");
		thrown = ExpectedException.none();
		
		// test informat
		set = nifParameterFactory.constructFromHttp("input", "text/turtle",
				"application/json+ld", "abc", "text/turtle", "text/turtle",
				"http://example.org");
		assertTrue(set.getInformat().equals(RDFSerialization.TURTLE));

		set = nifParameterFactory.constructFromHttp("input", null,
				"application/json+ld", "abc", "text/turtle", "application/json+ld",
				"http://example.org");
		assertTrue(set.getInformat().equals(RDFSerialization.JSON_LD));
		
		set = nifParameterFactory.constructFromHttp("input", null,
				null, "abc", null, null,
				"http://example.org");
		assertTrue(set.getInformat().equals(RDFSerialization.TURTLE));
		
		// test outformat
		set = nifParameterFactory.constructFromHttp("input", "text/turtle",
				"application/json+ld", "abc", "application/json+ld", "text/turtle",
				"http://example.org");
		assertTrue(set.getInformat().equals(RDFSerialization.TURTLE));

		set = nifParameterFactory.constructFromHttp("input", null,
				null, "abc", "application/json+ld", "text/turtle",
				"http://example.org");
		assertTrue(set.getInformat().equals(RDFSerialization.JSON_LD));

		set = nifParameterFactory.constructFromHttp("input", "text/turtle",
				null, "abc", null, "text/turtle",
				"http://example.org");
		assertTrue(set.getInformat().equals(RDFSerialization.TURTLE));
	}
}