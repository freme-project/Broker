package eu.freme.broker.security.voter;

import eu.freme.broker.security.database.OwnedResource;
import eu.freme.broker.security.database.Template;
import eu.freme.broker.security.database.User;
import org.hibernate.metamodel.domain.Entity;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class TemplateAccessDecisionVoter implements AccessDecisionVoter<Object> {

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

		try {
			Template template = (Template) object;

			//temporary
			System.out.println("Successfully casted from Object to Template");


			if (authentication.getPrincipal().equals("anonymousUser")) {
				return ACCESS_DENIED;
			}

			User authenticatedUser = (User) authentication.getPrincipal();

			if (authenticatedUser.getRole().equals(User.roleAdmin)) {
				return ACCESS_GRANTED;
			} else if (template.getAccessLevel().equals(OwnedResource.AccessLevel.PUBLIC)) {
				return ACCESS_GRANTED;
			} else if (authenticatedUser.getName().equals(template.getOwner().getName())) {
				return ACCESS_GRANTED;
			} else {
				return ACCESS_DENIED;
			}
		} catch (ClassCastException e) {
			//temporary
			System.out.println("Handled ClassCastException from some Object to Template");
			return ACCESS_ABSTAIN;
		}
	}
}
