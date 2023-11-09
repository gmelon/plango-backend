package dev.gmelon.plango.config.auth.jwt;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.config.auth.dto.MemberPrincipal;
import dev.gmelon.plango.config.auth.dto.TokenResponseDto;
import dev.gmelon.plango.exception.auth.JWTException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JWTProvider implements InitializingBean {
    private static final String EMAIL_KEY = "email";
    private static final String AUTHORITIES_KEY = "authorities";
    private static final int ACCESS_TOKEN_EXPIRATION_MINUTES = 30;
    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;
    private static final String AUTHORITIES_DELIMETER = ",";
    private static final String EMPTY_CREDENTIALS = "";

    private final Clock clock;

    @Value("${jwt-key}")
    private String encodedKeyValue;
    private SecretKey key;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Base64.getDecoder().decode(encodedKeyValue);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenResponseDto createToken(Authentication authentication) {
        LocalDateTime accessTokenExpiration = LocalDateTime.now(clock).plusMinutes(ACCESS_TOKEN_EXPIRATION_MINUTES);
        LocalDateTime refreshTokenExpiration = LocalDateTime.now(clock).plusDays(REFRESH_TOKEN_EXPIRATION_DAYS);

        return TokenResponseDto.builder()
                .accessToken(accessToken(authentication, accessTokenExpiration))
                .accessTokenExpiration(accessTokenExpiration)
                .refreshToken(refreshToken(authentication, refreshTokenExpiration))
                .refreshTokenExpiration(refreshTokenExpiration)
                .build();
    }

    private String accessToken(Authentication authentication, LocalDateTime accessTokenExpiration) {
        return Jwts.builder()
                .claim(EMAIL_KEY, authentication.getName())
                .claim(AUTHORITIES_KEY, joinedAuthorities(authentication))
                .issuedAt(Timestamp.valueOf(LocalDateTime.now(clock)))
                .expiration(Timestamp.valueOf(accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    private String joinedAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(joining(AUTHORITIES_DELIMETER));
    }

    private String refreshToken(Authentication authentication, LocalDateTime refreshTokenExpiration) {
        return Jwts.builder()
                .claim(EMAIL_KEY, authentication.getName())
                .issuedAt(Timestamp.valueOf(LocalDateTime.now(clock)))
                .expiration(Timestamp.valueOf(refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * Access Token을 파싱하여 Authentication을 생성한다.
     */
    public Authentication parseAccessToken(String accessToken) {
        Claims payload = parseToken(accessToken);

        List<GrantedAuthority> authorities = parseAuthorities(payload);
        MemberPrincipal principal = MemberPrincipal.of(payload.get(EMAIL_KEY, String.class),
                authorities);
        return UsernamePasswordAuthenticationToken.authenticated(principal, EMPTY_CREDENTIALS, authorities);
    }

    private List<GrantedAuthority> parseAuthorities(Claims payload) {
        return Arrays.stream(payload.get(AUTHORITIES_KEY, String.class).split(AUTHORITIES_DELIMETER))
                .map(SimpleGrantedAuthority::new)
                .collect(toList());
    }

    /**
     * Refresh Token을 파싱하여 회원의 email을 반환한다.
     */
    public String parseRefreshToken(String refreshToken) {
        Claims payload = parseToken(refreshToken);

        return payload.get(EMAIL_KEY, String.class);
    }

    private Claims parseToken(String token) {
        Claims payload;
        try {
            payload = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (UnsupportedJwtException exception) {
            throw new JWTException("지원하지 않는 토큰입니다.");
        } catch (JwtException | IllegalArgumentException exception) {
            throw new JWTException("유효하지 않은 토큰입니다.");
        }
        return payload;
    }
}
