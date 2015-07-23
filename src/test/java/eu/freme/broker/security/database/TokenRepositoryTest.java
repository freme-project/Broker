package eu.freme.broker.security.database;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.freme.broker.BrokerConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class TokenRepositoryTest {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	TokenRepository tokenRepository;
	
	@Test
	public void testTokenRepository(){
		User user = new User("hallo", "welt", User.roleUser);
		userRepository.save(user);
		
		Token token = new Token("bla", user);
		tokenRepository.save(token);
	}
}
