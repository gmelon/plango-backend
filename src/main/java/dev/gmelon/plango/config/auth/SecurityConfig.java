package dev.gmelon.plango.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.auth.handler.CustomAccessDeniedHandler;
import dev.gmelon.plango.config.auth.handler.CustomAuthenticationEntryPoint;
import dev.gmelon.plango.config.auth.handler.CustomOauth2SuccessHandler;
import dev.gmelon.plango.config.auth.jwt.JWTAuthenticationFilter;
import dev.gmelon.plango.config.auth.jwt.JWTExceptionHandlerFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final CustomOauth2UserService customOauth2UserService;
    private final CustomOauth2SuccessHandler customOauth2SuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final JWTExceptionHandlerFilter jwtExceptionHandlerFilter;
    private final ObjectMapper objectMapper;

    @Value("${remember-me.token-key}")
    private String rememberMeTokenKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests()
                .antMatchers("/h2-console/**").permitAll() // TODO mvc로는 왜 안 되는지
                .mvcMatchers("/", "/error", "/favicon.ico", "/health").permitAll()
                .mvcMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/token-refresh").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionHandlerFilter, JWTAuthenticationFilter.class)
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper));
                    exception.accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper));
                })
                .headers().frameOptions().disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable() // TODO 확인
                .formLogin().disable()
                .httpBasic().disable()
                .oauth2Login(oauth2LoginCustomizer -> oauth2LoginCustomizer
                        .redirectionEndpoint(redirectionEndpointCustomizer ->
                                redirectionEndpointCustomizer.baseUri("/oauth2/login/code/**"))
                        .successHandler(customOauth2SuccessHandler)
                        .userInfoEndpoint()
                        .userService(customOauth2UserService)
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(provider);
    }

    @Bean
    public AuthorizationEventPublisher authorizationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        return new SpringAuthorizationEventPublisher(applicationEventPublisher);
    }

}
