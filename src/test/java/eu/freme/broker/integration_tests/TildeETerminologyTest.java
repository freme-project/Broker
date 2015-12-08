/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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
package eu.freme.broker.integration_tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;

/**
 * Test Tilde e-Terminology broker endpoint.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */

public class TildeETerminologyTest extends EServiceTest {

	public TildeETerminologyTest() {
		super("/e-terminology/tilde");
	}

	@Test
	public void testETerminology() throws Exception {

		String nif = readFile("src/test/resources/rdftest/e-terminology/example1.ttl");
		HttpResponse<String> response = post("")
				.queryString("source-lang", "en")
				.queryString("target-lang", "de")
				.queryString("informat", "turtle")
				.queryString("outformat", "turtle").body(nif).asString();

		assertTrue(response.getStatus() == HttpStatus.OK.value());

		// not working due to bug in tilde terminology api
		response = post("")
				.queryString("source-lang", "en")
				.queryString("target-lang", "de")
				.queryString("informat", "text")
				.queryString("outformat", "turtle").body("hello world")
				.asString();

		assertTrue(response.getStatus() == HttpStatus.OK.value());
	}
}
