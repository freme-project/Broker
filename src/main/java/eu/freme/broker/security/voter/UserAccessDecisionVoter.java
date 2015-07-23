package eu.freme.broker.security.voter;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import eu.freme.broker.security.database.User;

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
		
		System.err.println();
		if( authentication.getPrincipal().equals( "anonymousUser" )){
			return ACCESS_DENIED;
		}

		User authenticatedUser = (User) authentication.getDetails();
		if (authenticatedUser.getName() == object.getName()) {
			return ACCESS_GRANTED;
		} else {
			return ACCESS_DENIED;
		}
	}
}
