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
package eu.freme.broker.tools;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.freme.broker.FremeCommonConfig;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FremeCommonConfig.class)
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
