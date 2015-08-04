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
