package dev.gmelon.plango.global.config.auth.jwt;

import dev.gmelon.plango.domain.auth.exception.JWTException;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private final JWTProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeaderValue = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.nonNull(authorizationHeaderValue)) {
            String accessToken = parseAccessToken(authorizationHeaderValue);
            Authentication authentication = jwtProvider.parseAccessToken(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String parseAccessToken(String authorizationHeaderValue) {
        if (!authorizationHeaderValue.startsWith(BEARER_TOKEN_PREFIX)) {
            throw JWTException.unSupportedToken();
        }
        return authorizationHeaderValue.substring(BEARER_TOKEN_PREFIX.length()).trim();
    }
}
