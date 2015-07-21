package eu.freme.broker.security;

import java.util.List;

import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.common.security.database.User;
import eu.freme.common.security.database.UserRepository;
import eu.freme.common.security.tools.PasswordHasher;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;

public class DatabaseAuthenticator implements ExternalServiceAuthenticator {

	@Autowired
	UserRepository userRepository;
	
	Logger logger = Logger.getLogger(DatabaseAuthenticator.class);
	
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
        
    	AuthenticationWithToken auth = new AuthenticationWithToken(user, null, AuthorityUtils.commaSeparatedStringToAuthorityList(User.roleAdmin));
        return auth;
    }
}
