package eu.freme.broker.security.voter;

import eu.freme.broker.security.database.OwnedResource;
import eu.freme.broker.security.database.model.Dataset;
import eu.freme.broker.security.database.model.User;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class DatasetAccessDecisionVoter implements AccessDecisionVoter<Object> {

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Object.class;
	}

	@Override
	public int vote(Authentication authentication, Object object,
			Collection<ConfigAttribute> attributes) {
		if (object instanceof Dataset) {
			Dataset casted= (Dataset) object;
			if (authentication.getPrincipal().equals("anonymousUser")) {
				return ACCESS_DENIED;
			}

			User authenticatedUser = (User) authentication.getPrincipal();

			if (authenticatedUser.getRole().equals(User.roleAdmin)) {
				return ACCESS_GRANTED;
			} else if (casted.getAccessLevel().equals(OwnedResource.AccessLevel.PUBLIC)) {
				return ACCESS_GRANTED;
			} else if (authenticatedUser.getName().equals(casted.getOwner().getName())) {
				return ACCESS_GRANTED;
			} else {
				return ACCESS_DENIED;
			}

		} else return ACCESS_ABSTAIN;

	}
}
