package eu.freme.broker.security.database;

import eu.freme.broker.BrokerConfig;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Type;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class TemplateRepositoryTest {

	Logger logger = Logger.getLogger(TokenRepositoryTest.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	TemplateRepository templateRepository;

	@Test
	public void testUserRepository(){

		logger.info("create user and save it");
		User testuser= new User("Juergen", "bla", User.roleUser);
		userRepository.save(testuser);

		logger.info("create template \"1\" and save it");
		templateRepository.save(new Template("1", testuser, Template.AccessLevel.PUBLIC));
		logger.info("create template \"2\" and save it");
		templateRepository.save(new Template("2", testuser, Template.AccessLevel.PUBLIC));
		logger.info("create template \"3\" and save it");
		templateRepository.save(new Template("3", testuser, Template.AccessLevel.PUBLIC));

		logger.info("fetch template \"2\"");
		Template two = templateRepository.findOneById("2");
		assertTrue(two!=null);

		logger.info("count templates");
		int counter = Helper.count(templateRepository.findAll());
		// admin user is one more
		assertTrue(counter==3);

		logger.info("delete template \"2\"");
		templateRepository.delete(two);
		counter = Helper.count(templateRepository.findAll());
		assertTrue(counter==2);
	}
}
