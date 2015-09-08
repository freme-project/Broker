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
package eu.freme.broker.integration_tests;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import eu.freme.broker.FremeFullConfig;

/**
 * This class sets up FREME integration tests. It can start FREME but can be
 * configured by the TestRunner to not start FREME. It also provides the base
 * url for the API under test. In case FREME is not started by
 * IntegrationTestSetup then the base url for the API under test should be
 * specified. This is done by TestRunner class.
 * 
 * @author Jan Nehring
 */
public class IntegrationTestSetup {

	static ConfigurableApplicationContext context;
	/**
	 * Should freme be started by the integration test setup? This can be used
	 * to create an API testing tool.
	 */
	static boolean startFreme;

	static Logger logger = Logger.getLogger(IntegrationTestSetup.class);
	static boolean alreadySetup = false;

	public static void setUp() {

		logger.info("\n-------------------------------------------------------\nSetup FREME Integration tests\n-------------------------------------------------------");

		String str = System.getProperty("freme.test.startServer");
		if (str == null) {
			startFreme = true;
		} else {
			startFreme = new Boolean(str).booleanValue();
		}

		if (startFreme) {
			context = SpringApplication.run(FremeFullConfig.class);
		}

		alreadySetup = true;
		logger.info("\n-------------------------------------------------------\nSetup FREME Integration tests finished\n-------------------------------------------------------");

	}

	public static String getURLEndpoint() {

		if (!alreadySetup) {
			setUp();
		}

		String baseurl = System.getProperty("freme.test.baseurl");
		if (baseurl != null) {
			return baseurl;
		} else {
			String port = context.getEnvironment().getProperty("server.port");
			if (port == null) {
				throw new RuntimeException("property server.port is not set");
			}
			return "http://localhost:" + port;
		}
	}
	
	public static ConfigurableApplicationContext getApplicationContext(){
		return context;
	}

	public static ConfigurableApplicationContext getContext() {
		return context;
	}

	
}
