package eu.freme.broker.security.infrastructure;

import eu.freme.broker.security.infrastructure.externalwebservice.ExternalWebServiceStub;
import eu.freme.broker.security.infrastructure.security.AuthenticationWithToken;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AuthenticatedExternalWebService extends AuthenticationWithToken {

    private ExternalWebServiceStub externalWebService;

    public AuthenticatedExternalWebService(Object aPrincipal, Object aCredentials, Collection<? extends GrantedAuthority> anAuthorities) {
        super(aPrincipal, aCredentials, anAuthorities);
    }

    public void setExternalWebService(ExternalWebServiceStub externalWebService) {
        this.externalWebService = externalWebService;
    }

    public ExternalWebServiceStub getExternalWebService() {
        return externalWebService;
    }
}
