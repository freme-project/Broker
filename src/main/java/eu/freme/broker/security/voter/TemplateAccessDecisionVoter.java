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
package eu.freme.broker.security.voter;

import eu.freme.broker.security.database.OwnedResource;
import eu.freme.broker.security.database.model.Template;
import eu.freme.broker.security.database.model.User;

import eu.freme.broker.security.tools.AccessLevelHelper;
import org.hibernate.metamodel.domain.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class TemplateAccessDecisionVoter implements AccessDecisionVoter<Object> {

	@Autowired
	AccessLevelHelper accessLevelHelper;

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class clazz) {
		return (clazz==Template.class);
	}

	@Override
	public int vote(Authentication authentication, Object object,
			Collection<ConfigAttribute> attributes) {
		//System.out.println("CAllal");
		try {
			Template template = (Template) object;

			//temporary
			//System.out.println("Successfully casted from Object to Template");



			if (authentication.getPrincipal().equals("anonymousUser")) {
				return ACCESS_DENIED;
			}

			User authenticatedUser = (User) authentication.getPrincipal();
			if (authenticatedUser.getRole().equals(User.roleAdmin)) {
				return ACCESS_GRANTED;
			} else if (template.getAccessLevel().equals(OwnedResource.AccessLevel.PUBLIC) && accessLevelHelper.hasRead(attributes)) {
				return ACCESS_GRANTED;
			} else if (authenticatedUser.getName().equals(template.getOwner().getName())) {
				return ACCESS_GRANTED;
			} else {
				return ACCESS_DENIED;
			}
		} catch (ClassCastException e) {
			//temporary
			return ACCESS_ABSTAIN;
		}
	}
}
