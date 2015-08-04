/**
 * Copyright (C) 2015 Felix Sasaki (Felix.Sasaki@dfki.de)
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
package eu.freme.broker.security.voter;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import eu.freme.broker.security.database.User;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class UserAccessDecisionVoter implements AccessDecisionVoter<User> {

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == User.class;
	}

	@Override
	public int vote(Authentication authentication, User object,
			Collection<ConfigAttribute> attributes) {
		
		if( authentication.getPrincipal().equals( "anonymousUser" )){
			return ACCESS_DENIED;
		}

		User authenticatedUser = (User) authentication.getPrincipal();
		
		if( authenticatedUser.getRole().equals(User.roleAdmin)){
			return ACCESS_GRANTED;
		} else if (authenticatedUser.getName().equals(object.getName())) {
			return ACCESS_GRANTED;
		} else {
			return ACCESS_DENIED;
		}
	}
}
