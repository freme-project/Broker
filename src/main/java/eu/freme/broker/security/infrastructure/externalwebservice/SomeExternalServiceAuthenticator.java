package eu.freme.broker.security.infrastructure.externalwebservice;

import java.util.List;

import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.security.domain.DomainUser;
import eu.freme.broker.security.infrastructure.AuthenticatedExternalWebService;
import eu.freme.broker.security.infrastructure.security.AuthenticationWithToken;
import eu.freme.broker.security.infrastructure.security.ExternalServiceAuthenticator;
import eu.freme.common.security.database.User;
import eu.freme.common.security.database.UserRepository;
import eu.freme.common.security.tools.PasswordHasher;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;

public class SomeExternalServiceAuthenticator implements ExternalServiceAuthenticator {

	@Autowired
	UserRepository userRepository;
	
	Logger logger = Logger.getLogger(SomeExternalServiceAuthenticator.class);
	
    @Override
    public AuthenticationWithToken authenticate(String username, String password) {
        
    	List<User> userList = userRepository.findByName(username);
    	if( userList.size() == 0 ){
    		throw new BadCredentialsException("authentication failed");
    	}
    	User user = userList.get(0);
    	
    	try {
			if( !PasswordHasher.check(password, user.getPassword()) ){
				throw new BadCredentialsException("authentication failed");    		
			}
		} catch( BadCredentialsException e ){
			throw e;
		} catch (Exception e) {
			logger.error(e);
			throw new InternalServerErrorException();
		}
        
    	AuthenticationWithToken auth = new AuthenticationWithToken(new DomainUser(username), null, AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_DOMAIN_USER"));

        // If authentication to external service succeeded then create authenticated wrapper with proper Principal and GrantedAuthorities.
        // GrantedAuthorities may come from external service authentication or be hardcoded at our layer as they are here with ROLE_DOMAIN_USER
        //AuthenticatedExternalWebService authenticatedExternalWebService = new AuthenticatedExternalWebService(new DomainUser(username), null,
       // authenticatedExternalWebService.setExternalWebService(externalWebService);

        return auth;
    }
}
