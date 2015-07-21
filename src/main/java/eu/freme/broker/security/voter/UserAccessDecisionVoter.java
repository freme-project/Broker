package eu.freme.broker.security.voter;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import eu.freme.common.security.database.User;

public class UserAccessDecisionVoter implements AccessDecisionVoter<User>{

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		System.err.println("****************************");
		System.err.println(clazz);
		System.err.println("****************************");
		return clazz == User.class;
	}

	@Override
	public int vote(Authentication authentication, User object,
			Collection<ConfigAttribute> attributes) {
		System.err.println("****************************");
		System.err.println(authentication.getPrincipal());
		System.err.println("****************************");
		return ACCESS_DENIED;
	}

}
