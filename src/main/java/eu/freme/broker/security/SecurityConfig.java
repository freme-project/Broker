package eu.freme.broker.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import eu.freme.broker.security.token.TokenAuthenticationProvider;
import eu.freme.broker.security.token.TokenRepository;
import eu.freme.broker.security.token.TokenService;
import eu.freme.broker.security.voter.UserAccessDecisionVoter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebMvcSecurity
@EnableScheduling
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ApplicationContextAware{

    @Value("${backend.admin.role}")
    private String backendAdminRole;
    
    private ApplicationContext applicationContext;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                csrf().disable().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().
                authorizeRequests().anyRequest().anonymous().
                and().
                exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint());

        http.addFilterBefore(new AuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class).
                addFilterBefore(new ManagementEndpointAuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.// authenticationProvider(domainUsernamePasswordAuthenticationProvider()).
                authenticationProvider(backendAdminUsernamePasswordAuthenticationProvider()).
                authenticationProvider(tokenAuthenticationProvider());
    }

    @Bean
    public TokenService tokenService() {
        return new TokenService();
    }

    @Bean
    public ExternalServiceAuthenticator someExternalServiceAuthenticator() {
        return new DatabaseAuthenticator();
    }

//    @Bean
//    public AuthenticationProvider domainUsernamePasswordAuthenticationProvider() {
//        return new DomainUsernamePasswordAuthenticationProvider(tokenService(), someExternalServiceAuthenticator());
//    }

    @Bean
    public AuthenticationProvider backendAdminUsernamePasswordAuthenticationProvider() {
        return new BackendAdminUsernamePasswordAuthenticationProvider();
    }

    @Bean
    public AuthenticationProvider tokenAuthenticationProvider() {
        return new TokenAuthenticationProvider(tokenService());
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
    	return new AuthenticationEntryPoint() {
			
			@Override
			public void commence(HttpServletRequest request,
					HttpServletResponse response, AuthenticationException authException)
					throws IOException, ServletException {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		};
    }
    
    public void setApplicationContext(ApplicationContext applicationContext){
    	super.setApplicationContext(applicationContext);
    	this.applicationContext = applicationContext;
    }
    
    @Bean
    public AffirmativeBased getDecisionVoter(){
    	Map<String,AccessDecisionVoter> map = applicationContext.getBeansOfType(AccessDecisionVoter.class);
    	ArrayList<AccessDecisionVoter> list = new ArrayList<AccessDecisionVoter>(map.values());
    	list.add(new UserAccessDecisionVoter());
    	AffirmativeBased ab = new AffirmativeBased(list);
    	return ab;
    }
    
}