/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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
package eu.freme.broker.security.database;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import eu.freme.broker.FremeCommonConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FremeCommonConfig.class)
@ActiveProfiles("broker")
@Ignore
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
