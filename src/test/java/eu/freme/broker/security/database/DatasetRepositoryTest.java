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
import eu.freme.broker.security.database.dao.DatasetDAO;
import eu.freme.broker.security.database.dao.UserDAO;
import eu.freme.broker.security.database.model.Dataset;
import eu.freme.broker.security.database.model.User;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class DatasetRepositoryTest {

	Logger logger = Logger.getLogger(TokenRepositoryTest.class);

	@Autowired
	UserDAO userDAO;

	@Autowired
	DatasetDAO datasetDAO;

	@Test
	@Transactional
	public void testDatasetRepository() {

		logger.info("create user and save it");
		User testuser = new User("Juergen", "bla", User.roleUser);
		userDAO.save(testuser);

		logger.info("create dataset \"1\" and save it");
		datasetDAO.save(new Dataset("1", testuser,
				OwnedResource.AccessLevel.PUBLIC));
		logger.info("create dataset \"2\" and save it");
		datasetDAO.save(new Dataset("2", testuser,
				OwnedResource.AccessLevel.PUBLIC));
		logger.info("create dataset \"3\" and save it");
		datasetDAO.save(new Dataset("3", testuser,
				OwnedResource.AccessLevel.PUBLIC));

		logger.info("fetch dataset \"2\"");
		Dataset two = datasetDAO.getRepository().findOneById("2");
		assertTrue(two != null);

		logger.info("count datasets");
		int counter = Helper.count(datasetDAO.findAll());
		// admin user is one more
		assertTrue(counter == 3);

		logger.info("delete dataset \"2\"");
		datasetDAO.delete(two);
		counter = Helper.count(datasetDAO.findAll());
		assertTrue(counter == 2);

		User userFromDb = userDAO.getRepository().findOneByName(testuser.getName());
		userDAO.delete(userFromDb);
		counter = Helper.count(datasetDAO.findAll());
		assertTrue(counter == 0);

	}

	@Test	
	@Transactional
	public void testDatasetRepository2() {

		logger.info("create user and dataset");
		User user = new User("hallo", "welt", User.roleUser);
		userDAO.save(user);
		
		Dataset dataset = new Dataset("t1", user,
				OwnedResource.AccessLevel.PUBLIC);
		datasetDAO.save(dataset);

		assertTrue(datasetDAO.findAll().iterator().hasNext());
		assertTrue(userDAO.findAll().iterator().hasNext());

		logger.info("load dataset, see if it has the right user attached");
		Dataset fromDb = datasetDAO.getRepository().findOneById(dataset.getId());
		assertTrue(fromDb.getOwner().getName().equals(user.getName()));

		logger.info("dataset count: "
				+ Helper.count(datasetDAO.findAll()));
		logger.info("create 2nd dataset and delete 1st");
		Dataset dataset2 = new Dataset("t2", user,
				OwnedResource.AccessLevel.PUBLIC);
		datasetDAO.save(dataset2);
		logger.info("dataset count (before delete): "
				+ Helper.count(datasetDAO.findAll()));
		datasetDAO.delete(dataset);

		logger.info("dataset count (after delete): " + Helper.count(datasetDAO.findAll()));
		assertEquals(1, Helper.count(datasetDAO.findAll()));

		assertTrue(datasetDAO.findAll().iterator().hasNext());
		assertTrue(userDAO.findAll().iterator().hasNext());

		User userFromDb = userDAO.getRepository().findOneByName(user.getName());
		logger.info("delete user, should delete dataset also");
		userDAO.delete(userFromDb);
		assertFalse(datasetDAO.findAll().iterator().hasNext());

		assertTrue(userDAO.count() == 1L); // just the admin



	}
}
