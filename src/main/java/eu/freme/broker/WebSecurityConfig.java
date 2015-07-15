//package eu.freme.broker;
//
//import java.io.IOException;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//
//@Configuration
//@EnableWebMvcSecurity
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//	@Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//            .authorizeRequests()
//                .antMatchers("*").permitAll();
//        
//        
//        http.csrf().disable().
//        sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
//        and().
//        authorizeRequests().
//        antMatchers(actuatorEndpoints()).hasRole(backendAdminRole).
//        anyRequest().authenticated().
//        and().
//        anonymous().disable().
//        exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint());
//
//http.addFilterBefore(new AuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class).
//        addFilterBefore(new ManagementEndpointAuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class);
//
//    }
//	
//	@Bean
//	public AuthenticationEntryPoint unauthorizedEntryPoint() {
//		return new AuthenticationEntryPoint() {			
//			@Override
//			public void commence(HttpServletRequest request,
//					HttpServletResponse response, AuthenticationException authException)
//					throws IOException, ServletException {
//				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//			}
//		};
//	}
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//            .inMemoryAuthentication()
//                .withUser("user").password("password").roles("USER");
//    }
//}
