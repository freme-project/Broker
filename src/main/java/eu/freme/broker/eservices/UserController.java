package eu.freme.broker.eservices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	public User createUser(
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password) {

		if (userRepository.findByName(username).size() > 0) {
			throw new BadRequestException("Username already exists");
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

	@RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE)
	// @PreAuthorize("hasRole('" + User.roleUser + "')")
	public ResponseEntity<String> deleteUser(@PathVariable("userId") long userId) {

		User user = userRepository.findOne((long) userId);
		if (user == null) {
			throw new BadRequestException("User not found");
		}

		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		decisionManager.decide(authentication, user, null);
		userRepository.delete(user);

		return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
	}

	public void setDecisionManager(AbstractAccessDecisionManager decisionManager) {
		this.decisionManager = decisionManager;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}
}
