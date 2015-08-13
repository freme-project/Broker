package eu.freme.broker.security.database;

import eu.freme.broker.BrokerConfig;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class DatasetRepositoryTest {

	Logger logger = Logger.getLogger(TokenRepositoryTest.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	DatasetRepository datasetRepository;

	@Test
	public void testUserRepository(){

		logger.info("create user and save it");
		User testuser= new User("Juergen", "bla", User.roleUser);
		userRepository.save(testuser);

		logger.info("create dataset \"1\" and save it");
		datasetRepository.save(new Dataset("1", testuser, OwnedResource.AccessLevel.PUBLIC));
		logger.info("create dataset \"2\" and save it");
		datasetRepository.save(new Dataset("2", testuser, OwnedResource.AccessLevel.PUBLIC));
		logger.info("create dataset \"3\" and save it");
		datasetRepository.save(new Dataset("3", testuser, OwnedResource.AccessLevel.PUBLIC));

		logger.info("fetch dataset \"2\"");
		Dataset two = datasetRepository.findOneById("2");
		assertTrue(two!=null);

		logger.info("count datasets");
		int counter = Helper.count(datasetRepository.findAll());
		// admin user is one more
		assertTrue(counter==3);

		logger.info("delete dataset \"2\"");
		datasetRepository.delete(two);
		counter = Helper.count(datasetRepository.findAll());
		assertTrue(counter==2);
	}
}
