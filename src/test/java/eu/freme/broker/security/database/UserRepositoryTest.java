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
import eu.freme.broker.security.database.dao.UserDAO;
import eu.freme.broker.security.database.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class UserRepositoryTest {

	@Autowired
	UserDAO userDAO;
	
	@Test
	@Transactional
	public void testUserRepository(){


		int preexisting = Helper.count(userDAO.findAll());
		userDAO.save(new User("Juergen", "bla", User.roleUser));
		userDAO.save(new User("Peter", "bla", User.roleUser));
		userDAO.save(new User("Madeleine", "bla", User.roleAdmin));
		
		User juergen = userDAO.getRepository().findOneByName("Juergen");
		assertTrue(juergen!=null);
		
		int counter = Helper.count(userDAO.findAll());
		// admin user is one more

		assertTrue(counter==(preexisting+3));

		userDAO.delete(juergen);
		counter = Helper.count(userDAO.findAll());
		assertTrue(counter==(preexisting+2));
	}
}
