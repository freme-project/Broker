package eu.freme.broker.integration_tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
