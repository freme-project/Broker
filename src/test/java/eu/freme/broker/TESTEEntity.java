package eu.freme.broker;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jan Nehring
 */
public class TESTEEntity {

	@Test
	public void test(){
		String endpoint = APIFullTest.getURLEndpoint();
		assertTrue(endpoint != null);
	}
}
