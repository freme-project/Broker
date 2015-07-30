package eu.freme.broker.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;

public class FREMEAuthenticationManager implements AuthenticationManager {

	@Autowired
	AuthenticationProvider[] authenticationProviders;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws ProviderNotFoundException {

		for( AuthenticationProvider auth : authenticationProviders){
			if( auth.supports(authentication.getClass())){
				return auth.authenticate(authentication);
			}
		}
		
		throw new ProviderNotFoundException("No AuthenticationProvider found for " + authentication.getClass());
	}
}
