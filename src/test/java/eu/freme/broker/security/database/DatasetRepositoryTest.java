package eu.freme.broker.security.database;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import eu.freme.broker.BrokerConfig;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class DatasetRepositoryTest {

	Logger logger = Logger.getLogger(TokenRepositoryTest.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	DatasetRepository datasetRepository;

	@PersistenceContext
	EntityManager entityManager;

	@Test
	public void testDatasetRepository() {

		logger.info("create user and save it");
		User testuser = new User("Juergen", "bla", User.roleUser);
		userRepository.save(testuser);
		
		entityManager.flush();

		logger.info("create dataset \"1\" and save it");
		datasetRepository.save(new Dataset("1", testuser,
				OwnedResource.AccessLevel.PUBLIC));
		logger.info("create dataset \"2\" and save it");
		datasetRepository.save(new Dataset("2", testuser,
				OwnedResource.AccessLevel.PUBLIC));
		logger.info("create dataset \"3\" and save it");
		datasetRepository.save(new Dataset("3", testuser,
				OwnedResource.AccessLevel.PUBLIC));

		logger.info("fetch dataset \"2\"");
		Dataset two = datasetRepository.findOneById("2");
		assertTrue(two != null);

		logger.info("count datasets");
		int counter = Helper.count(datasetRepository.findAll());
		// admin user is one more
		assertTrue(counter == 3);

		logger.info("delete dataset \"2\"");
		datasetRepository.delete(two);
		counter = Helper.count(datasetRepository.findAll());
		assertTrue(counter == 2);

		userRepository.delete(testuser);
		counter = Helper.count(datasetRepository.findAll());
		assertTrue(counter == 0);

	}

	@Test	
	@Transactional
	public void testDatasetRepository2() {

		logger.info("create user and dataset");
		User user = new User("hallo", "welt", User.roleUser);
		userRepository.save(user);
		
		Dataset dataset = new Dataset("t1", user,
				OwnedResource.AccessLevel.PUBLIC);
		datasetRepository.save(dataset);

		assertTrue(datasetRepository.findAll().iterator().hasNext());
		assertTrue(userRepository.findAll().iterator().hasNext());

		logger.info("load dataset, see if it has the right user attached");
		Dataset fromDb = datasetRepository.findOneById(dataset.getId());
		assertTrue(fromDb.getOwner().getName().equals(user.getName()));

		logger.info("dataset count: "
				+ Helper.count(datasetRepository.findAll()));
		logger.info("create 2nd dataset and delete 1st");
		Dataset dataset2 = new Dataset("t2", user,
				OwnedResource.AccessLevel.PUBLIC);
		datasetRepository.save(dataset2);
		logger.info("dataset count (before delete): "
				+ Helper.count(datasetRepository.findAll()));
		datasetRepository.delete(dataset);

		entityManager.flush();
		logger.info("dataset count (after delete): " + Helper.count(datasetRepository.findAll()));
		assertEquals(1, Helper.count(datasetRepository.findAll()));

		assertTrue(datasetRepository.findAll().iterator().hasNext());
		assertTrue(userRepository.findAll().iterator().hasNext());

		User userFromDb = userRepository.findOneByName(user.getName());
		logger.info("delete user, should delete token also");
		userRepository.delete(userFromDb);
		entityManager.flush();
		assertFalse(userRepository.findAll().iterator().hasNext());

		assertFalse(datasetRepository.findAll().iterator().hasNext());

	}
}
