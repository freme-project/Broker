package eu.freme.broker.security.infrastructure.security.voter;

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
		return clazz == User.class;
	}

	@Override
	public int vote(Authentication authentication, User object,
			Collection<ConfigAttribute> attributes) {
		System.err.println(authentication.getPrincipal());
		return 0;
	}

}
