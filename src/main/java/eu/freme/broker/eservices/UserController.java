package eu.freme.broker.eservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.common.security.database.User;
import eu.freme.common.security.database.UserRepository;
import eu.freme.common.security.tools.PasswordHasher;

@RestController
public class UserController extends BaseRestController {

	@Autowired
	AbstractAccessDecisionManager decisionManager;

	@Autowired
	UserRepository userRepository;

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public User creatUser(
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password) {

		if (userRepository.findByName(username).size() > 0) {
			throw new BadRequestException("Username already exists");
		}
		
		try {
			String hashedPassword = PasswordHasher.getSaltedHash(password);User user = new User(username, hashedPassword, User.Role.User);
			userRepository.save(user);
			return user;
		} catch (Exception e) {
			logger.error(e);
			throw new InternalServerErrorException();
		}
	}
	
	@RequestMapping(value = "/auth-call", method = RequestMethod.POST)
	public ResponseEntity<String> auth() {

		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		User user = new User("bla", "blub", User.Role.Admin);

		decisionManager.decide(authentication, user, null);

		return new ResponseEntity<String>("hello", HttpStatus.OK);
	}

	public void setDecisionManager(AbstractAccessDecisionManager decisionManager) {
		this.decisionManager = decisionManager;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}
}
