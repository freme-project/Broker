/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import eu.freme.broker.BrokerConfig;
import eu.freme.broker.security.database.dao.UserDAO;
import eu.freme.broker.security.database.model.Token;
import eu.freme.broker.security.database.model.User;
import eu.freme.broker.security.database.repository.TokenRepository;
import eu.freme.broker.security.database.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class TokenRepositoryTest {

	Logger logger = Logger.getLogger(TokenRepositoryTest.class);
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	TokenRepository tokenRepository;
	
	@Autowired
	UserDAO userDAO;

	@PersistenceContext
	EntityManager entityManager;
	
	@Test
	@Transactional
	public void testTokenRepository(){
		entityManager.flush();
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

		logger.info("token count: " + Helper.count(tokenRepository.findAll()));
		logger.info("create 2nd token and delete 1st");
		Token token2 = new Token("t2", user);
		tokenRepository.save(token2);
		logger.info("token count (before delete): " + tokenRepository.count());
		assertEquals((long)2, tokenRepository.count());
		tokenRepository.delete(token);
		logger.info("token count (after delete): " + tokenRepository.count());

		assertEquals((long)1, tokenRepository.count());
		// one user is automatically generated admin user
		assertEquals((long)2, userRepository.count());
		
		entityManager.flush();
		entityManager.clear();

		User userFromDb = userRepository.findOneByName(user.getName());
		entityManager.flush();
		logger.info("delete user, should delete token also");
		userDAO.deleteUser(userFromDb);
		logger.info("token count (after user delete): " + tokenRepository.count());
		entityManager.flush();

		assertFalse(tokenRepository.findAll().iterator().hasNext());
	}
}
