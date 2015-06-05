package eu.freme.broker;

import org.junit.runner.JUnitCore;

/**
 * Starter class to execute test suite from a .jar archive
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
