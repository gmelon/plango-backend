package dev.gmelon.plango.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.auth.handler.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@RequiredArgsConstructor
@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests()
                .antMatchers("/", "/h2-console/**", "/error", "/favicon.ico", "/health").permitAll()
                .antMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/logout").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jsonEmailPasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper));
                    exception.accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper));
                })
                .logout()
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(new CustomLogoutSuccessHandler())
                .and()
                .headers().frameOptions().disable()
                .and()
                .csrf().disable() // TODO 확인
                .formLogin().disable()
                .httpBasic().disable()
                .build();
    }

    @Bean
    public JsonEmailPasswordAuthenticationFilter jsonEmailPasswordAuthenticationFilter() {
        JsonEmailPasswordAuthenticationFilter filter = new JsonEmailPasswordAuthenticationFilter("/api/auth/login", objectMapper);
        filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        filter.setAuthenticationManager(authenticationManager());

//        TokenBasedRememberMeServices rememberMeServices = new TokenBasedRememberMeServices("remember-me", customUserDetailsService);
//        rememberMeServices.setAlwaysRemember(true);
//        rememberMeServices.setTokenValiditySeconds(2592000); // 30days
//        filter.setRememberMeServices(rememberMeServices);

        //        filter.setRememberMeServices(new SpringSessionRememberMeServices());

        filter.setAuthenticationSuccessHandler(new CustomAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(new CustomAuthenticationFailureHandler(objectMapper));

        return filter;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(provider);
    }

    @Bean
    public AuthorizationEventPublisher authorizationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringAuthorizationEventPublisher(applicationEventPublisher);
    }

}
