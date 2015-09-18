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
import eu.freme.broker.security.database.dao.TemplateSecurityDAO;
import eu.freme.broker.security.database.dao.UserDAO;
import eu.freme.broker.security.database.model.Template;
import eu.freme.broker.security.database.model.User;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class TemplateRepositoryTest {

	Logger logger = Logger.getLogger(TokenRepositoryTest.class);

	@Autowired
	UserDAO userDAO;

	@Autowired
	TemplateSecurityDAO templateSecurityDAO;

	@Test
	@Transactional
	public void testTemplateRepository(){

		logger.info("create user and save it");
		User testuser = new User("Juergen", "bla", User.roleUser);
		userDAO.save(testuser);

		logger.info("create template \"1\" and save it");
		templateSecurityDAO.save(new Template("1", testuser, OwnedResource.AccessLevel.PUBLIC));
		logger.info("create template \"2\" and save it");
		templateSecurityDAO.save(new Template("2", testuser, OwnedResource.AccessLevel.PUBLIC));
		logger.info("create template \"3\" and save it");
		templateSecurityDAO.save(new Template("3", testuser, OwnedResource.AccessLevel.PUBLIC));

		logger.info("fetch template \"2\"");
		Template two = templateSecurityDAO.getRepository().findOneById("2");
		assertTrue(two != null);

		logger.info("count templates");
		// admin user is one more
		assertTrue(templateSecurityDAO.count() == 3L);

		logger.info("delete template \"2\"");

		templateSecurityDAO.delete(two);

		assertTrue(templateSecurityDAO.count() == 2L);


		logger.info("delete user, should delete template also");
		User userFromDb = userDAO.getRepository().findOneByName(testuser.getName());
		userDAO.delete(userFromDb);
		logger.info("remaining templates: "+templateSecurityDAO.count());
		assertFalse(templateSecurityDAO.findAll().iterator().hasNext());
	}
}
