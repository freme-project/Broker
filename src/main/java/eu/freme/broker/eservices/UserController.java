/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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
package eu.freme.broker.eservices;

import eu.freme.common.persistence.tools.AccessLevelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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
import eu.freme.broker.security.tools.PasswordHasher;
import eu.freme.common.persistence.model.User;
import eu.freme.common.persistence.repository.UserRepository;

@RestController
@Profile("broker")
public class UserController extends BaseRestController {

	@Autowired
	AbstractAccessDecisionManager decisionManager;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	AccessLevelHelper accessLevelHelper;

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

	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_USER')")
	public User getUser(@PathVariable("username") String username) {

		User user = userRepository.findOneByName(username);
		if (user == null) {
			throw new BadRequestException("User not found");
		}

		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		decisionManager.decide(authentication, user, accessLevelHelper.readAccess());
		return user;
	} 

	@RequestMapping(value = "/user/{username}", method = RequestMethod.DELETE)
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> deleteUser(@PathVariable("username") String username) {

		User user = userRepository.findOneByName(username);
		if (user == null) {
			throw new BadRequestException("User not found");
		}

		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		decisionManager.decide(authentication, user, accessLevelHelper.writeAccess());
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
