package eu.freme.broker.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import eu.freme.broker.security.database.Token;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@SuppressWarnings("serial")
public class AuthenticationWithToken extends PreAuthenticatedAuthenticationToken {

	public AuthenticationWithToken(Object aPrincipal, Object aCredentials, Collection<? extends GrantedAuthority> anAuthorities, Token token) {
        super(aPrincipal, aCredentials, anAuthorities);
        setToken(token);
    }

    public void setToken(Token token) {
        setDetails(token);
    }

    public Token getToken() {
        return (Token)getDetails();
    }
}
