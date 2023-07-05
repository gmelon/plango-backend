package dev.gmelon.plango.config;

import dev.gmelon.plango.auth.AuthCheckInterceptor;
import dev.gmelon.plango.auth.LoginMemberArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthCheckInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/h2-console", "/error", "*.ico", "/health")
                .excludePathPatterns("/api/v1/auth/signup","/api/v1/auth/login", "/api/v1/auth/logout");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }
}
