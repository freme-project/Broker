/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * 					Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * 					Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RunWith(Suite.class)
@SuiteClasses({ TildeETranslationTest.class, // TildeETerminologyTest.class,
		TildeETranslationTest.class, FremeNERTest.class, ELinkTest.class })
public class TestRunner {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out
					.println("Missing baseurl parameter. Usage\n\njava eu.freme.broker http://api-under-test.org");
			return;
		}
		
		System.setProperty("freme.test.startServer", "false");
		System.setProperty("freme.test.baseurl", args[0]);
		JUnitCore.main("eu.freme.broker.integration_tests.TestRunner");
	}
}
