package eu.freme.broker.security.database;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import eu.freme.broker.BrokerConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class TokenRepositoryTest {

	Logger logger = Logger.getLogger(TokenRepositoryTest.class);
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	TokenRepository tokenRepository;
	
	@Test
	public void testTokenRepository(){
		
		logger.info("create user and token");
		User user = new User("hallo", "welt", User.roleUser);
		userRepository.save(user);
		
		Token token = new Token("t1", user);
		tokenRepository.save(token);
		
		assertTrue(tokenRepository.findAll().iterator().hasNext());
		assertTrue(userRepository.findAll().iterator().hasNext());
		
		logger.info("load token, see if it has the right user attached");
		Token fromDb = tokenRepository.findOneByToken(token.getToken());
		assertTrue(fromDb.getUser().getName().equals(user.getName()));
		
		logger.info("create 2nd token and delete 1st");
		Token token2 = new Token("t2", user);
		tokenRepository.save(token2);
		tokenRepository.delete(token);

		assertTrue(tokenRepository.findAll().iterator().hasNext());
		assertTrue(userRepository.findAll().iterator().hasNext());

		User userFromDb = userRepository.findOneByName(user.getName());
		logger.info("delete user, should delete token also");
		userRepository.delete(userFromDb);
		
		assertFalse(tokenRepository.findAll().iterator().hasNext());
	}
}
