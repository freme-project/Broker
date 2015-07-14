package eu.freme.broker.integration_tests;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import eu.freme.broker.FremeFullConfig;

/**
 * This class starts FREME for integration tests. It executes all test suites
 * from the @SuiteClasses annotation. It also provides the API base url (e.g.
 * http://localhost:8080) for the test suites.
 * 
 * @author Jan Nehring
 */
public class IntegrationTestBase {

	static ConfigurableApplicationContext context;
	/**
	 * Should freme be started by the integration test setup? This can be used
	 * to create an API testing tool.
	 */
	static boolean startFreme;

	static Logger logger = Logger.getLogger(IntegrationTestBase.class);

	@BeforeClass
	public static void setUp() {

		logger.info("---------------\nStart FREME Integration tests\n---------------");

		String str = System.getProperty("freme.test.startServer");
		if (str == null) {
			startFreme = true;
		} else {
			startFreme = new Boolean(str).booleanValue();
		}

		if (startFreme) {
			context = SpringApplication.run(FremeFullConfig.class);
		}
	}

	protected String getURLEndpoint() {

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

	@AfterClass
	public static void tearDown() {
		if (startFreme) {
			context.close();
		}
		logger.info("---------------\nFinished FREME Integration tests\n---------------");
	}
}
