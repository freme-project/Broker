//package eu.freme.broker.security;
//
//import com.google.common.base.Optional;
//
//import eu.freme.broker.security.token.Token;
//import eu.freme.broker.security.token.TokenService;
//
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//
//public class DomainUsernamePasswordAuthenticationProvider implements AuthenticationProvider {
//
//    private TokenService tokenService;
//    private ExternalServiceAuthenticator externalServiceAuthenticator;
//
//    public DomainUsernamePasswordAuthenticationProvider(TokenService tokenService, ExternalServiceAuthenticator externalServiceAuthenticator) {
//        this.tokenService = tokenService;
//        this.externalServiceAuthenticator = externalServiceAuthenticator;
//    }
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        Optional<String> username = (Optional) authentication.getPrincipal();
//        Optional<String> password = (Optional) authentication.getCredentials();
//
//        if (!username.isPresent() || !password.isPresent()) {
//            throw new BadCredentialsException("Invalid Domain User Credentials");
//        }
//
//        
//        String hashedPassword = 
//        
//        AuthenticationWithToken resultOfAuthentication = externalServiceAuthenticator.authenticate(username.get(), password.get());
//        Token newToken = tokenService.generateNewToken(user);
//        resultOfAuthentication.setToken(newToken);
//        tokenService.store(newToken, resultOfAuthentication);
//
//        return resultOfAuthentication;
//    }
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return authentication.equals(UsernamePasswordAuthenticationToken.class);
//    }
//}
