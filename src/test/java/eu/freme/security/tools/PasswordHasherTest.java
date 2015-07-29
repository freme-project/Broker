package eu.freme.security.tools;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.freme.broker.security.tools.PasswordHasher;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class PasswordHasherTest {

	private boolean checkPassword(String password) throws Exception {
		String hash = PasswordHasher.getSaltedHash(password);
		return PasswordHasher.check(password, hash);
	}

	@Test
	public void testPasswordHasher() throws Exception {
		assertTrue(checkPassword("abcdef"));
		assertTrue(checkPassword("_.'\"$!\\\"()(/)"));
		assertTrue(checkPassword("-"));
		try {
			checkPassword("");
			assertTrue(false);
		} catch (Exception e) {
		}
	}
}
