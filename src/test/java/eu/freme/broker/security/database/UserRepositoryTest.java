package eu.freme.broker.security.database;

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;
import eu.freme.broker.BrokerConfig;
import eu.freme.broker.security.database.User;
import eu.freme.broker.security.database.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BrokerConfig.class)
public class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;
	
	private int count(Iterable<User> itr){
		Iterator<User> itr2 = itr.iterator();
		int counter=0;
		while(itr2.hasNext()){
			counter++;
			itr2.next();
		}
		return counter;
	}
	
	@Test
	public void testUserRepository(){
		userRepository.save(new User("Juergen", "bla", User.roleUser));
		userRepository.save(new User("Peter", "bla", User.roleUser));
		userRepository.save(new User("Madeleine", "bla", User.roleAdmin));
		
		User juergen = userRepository.findByName("Juergen").get(0);
		assertTrue(juergen!=null);
		
		int counter = count(userRepository.findAll());
		assertTrue(counter==3);
		
		userRepository.delete(new Long(2));
		counter = count(userRepository.findAll());
		assertTrue(counter==2);
	}
}
