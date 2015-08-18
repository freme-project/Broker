package eu.freme.broker.security.voter;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import eu.freme.broker.security.database.User;
/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */
public class UserAccessDecisionVoter implements AccessDecisionVoter<Object> {

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == User.class;
	}

	@Override
	public int vote(Authentication authentication, Object object,
					Collection<ConfigAttribute> attributes) {
		try {
			User user = (User) object;

			//temporary
			System.out.println("Successfully casted from Object to User");

			if (authentication.getPrincipal().equals("anonymousUser")) {
				return ACCESS_DENIED;
			}

			User authenticatedUser = (User) authentication.getPrincipal();

			if (authenticatedUser.getRole().equals(User.roleAdmin)) {
				return ACCESS_GRANTED;
			} else if (authenticatedUser.getName().equals(user.getName())) {
				return ACCESS_GRANTED;
			} else {
				return ACCESS_DENIED;
			}
		} catch (ClassCastException e) {
			//temporary
			System.out.println("Handled ClassCastException from some Object to User");
			return ACCESS_ABSTAIN;
		}
	}
}