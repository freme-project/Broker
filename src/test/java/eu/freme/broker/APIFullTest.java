package eu.freme.broker;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This class starts FREME for integration tests. It executes all test suites
 * from the @SuiteClasses annotation. It also provides the API base url (e.g.
 * http://localhost:8080) for the test suites.
 * 
 * @author Jan Nehring
 */
@RunWith(Suite.class)
@SuiteClasses({ TestTildeETranslation.class, TESTEEntity.class })
public class APIFullTest {

	static ConfigurableApplicationContext context;
	static boolean startFreme;

	@BeforeClass
	public static void setUp() {
		System.err.println("xxx");
		String str = System.getProperty("freme.test.startServer");
		if( str == null ){
			startFreme = true;
		} else{
			startFreme = new Boolean(str).booleanValue();
		}
		
		if (startFreme) {
			context = SpringApplication.run(FremeFullConfig.class);
		}
	}

	static String getURLEndpoint() {
		
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
	}
}
