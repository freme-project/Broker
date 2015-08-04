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
package eu.freme.broker;

import org.junit.runner.JUnitCore;

/**
 * Starter class to execute test suite pointing to a particular endpoint.
 * 
 * @author Jan Nehring
 */
public class TestRunner {

	public static void main(String[] args) {
		
		if( args.length == 0 ){
			System.out.println("Missing baseurl parameter. Usage\n\njava eu.freme.broker http://api-under-test.org");
			return;
		}
		
		System.setProperty("freme.test.startServer", "false");
		System.setProperty("freme.test.baseurl", args[0]);
		JUnitCore.main("eu.freme.broker.APITestSuite");
	}
}
