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

import eu.freme.broker.BrokerConfig;
import eu.freme.broker.security.database.dao.TokenDAO;
import eu.freme.broker.security.database.dao.UserDAO;
import eu.freme.broker.security.database.model.Token;
import eu.freme.broker.security.database.model.User;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class TokenRepositoryTest {

	Logger logger = Logger.getLogger(TokenRepositoryTest.class);

	@Autowired
	UserDAO userDAO;

	@Autowired
	TokenDAO tokenDAO;
	
	@Autowired
	EntityManager entityManager;

	@Test
	@Transactional
	public void testTokenRepository(){
		//entityManager.flush();
		logger.info("create user and token");
		User user = new User("hallo", "welt", User.roleUser);
		userDAO.save(user);
		
		Token token = new Token("t1", user);
		tokenDAO.save(token);
		
		assertTrue(tokenDAO.findAll().iterator().hasNext());
		assertTrue(userDAO.findAll().iterator().hasNext());
		
		logger.info("load token, see if it has the right user attached");
		Token fromDb = tokenDAO.getRepository().findOneByToken(token.getToken());
		assertTrue(fromDb.getUser().getName().equals(user.getName()));

		logger.info("token count: " + Helper.count(tokenDAO.findAll()));
		logger.info("create 2nd token and delete 1st");
		Token token2 = new Token("t2", user);
		tokenDAO.save(token2);
		logger.info("token count (before delete): " + tokenDAO.count());
		assertEquals((long) 2, tokenDAO.count());
		tokenDAO.delete(token);
		logger.info("token count (after delete): " + tokenDAO.count());

		assertEquals((long) 1, tokenDAO.count());
		// one user is automatically generated admin user
		assertEquals((long) 2, userDAO.count());
		
		entityManager.flush();
		entityManager.clear();

		User userFromDb = userDAO.getRepository().findOneByName(user.getName());
		//entityManager.flush();
		logger.info("delete user, should delete token also");
		userDAO.delete(userFromDb);
		logger.info("token count (after user delete): " + tokenDAO.count());
		
		entityManager.flush();

		assertEquals((long) 0, tokenDAO.count());
	}
}
