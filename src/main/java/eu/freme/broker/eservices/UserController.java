package eu.freme.broker.eservices;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.security.database.User;
import eu.freme.broker.security.database.UserRepository;
import eu.freme.broker.security.tools.PasswordHasher;

@RestController
public class UserController extends BaseRestController {

	@Autowired
	AbstractAccessDecisionManager decisionManager;

	@Autowired
	UserRepository userRepository;

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public User createUser(
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password) {

		if (userRepository.findOneByName(username) != null) {
			throw new BadRequestException("Username already exists");
		}
		
		// validate that username consists only of charahters
		if( !username.matches("[a-zA-Z]+")){
			throw new BadRequestException("The username can only consist of normal characters from a-z and A-Z");
		}
		
		// passwords need to have at least 8 characters
		if( password.length() < 8 ){
			throw new BadRequestException("The passwords needs to be at least 8 characters long");
		}
		
		try {
			String hashedPassword = PasswordHasher.getSaltedHash(password);
			User user = new User(username, hashedPassword, User.roleUser);
			userRepository.save(user);
			return user;
		} catch (Exception e) {
			logger.error(e);
			throw new InternalServerErrorException();
		}
	}

	@RequestMapping(value = "/user/{username}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteUser(@PathVariable("username") String username) {

		User user = userRepository.findOneByName(username);
		if (user == null) {
			throw new BadRequestException("User not found");
		}

		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		decisionManager.decide(authentication, user, null);
		userRepository.delete(user);

		return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(value="/user", method= RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Iterable<User> getUsers(){
		
		return userRepository.findAll();
	}

	public void setDecisionManager(AbstractAccessDecisionManager decisionManager) {
		this.decisionManager = decisionManager;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}
}
