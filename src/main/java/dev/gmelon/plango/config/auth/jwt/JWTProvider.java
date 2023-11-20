package dev.gmelon.plango.config.auth.jwt;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.config.auth.dto.MemberPrincipal;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.service.auth.dto.TokenResponseDto;
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
import java.util.Collections;
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
    private static final String ID_KEY = "id";
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

    public TokenResponseDto createToken(Member member) {
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                new MemberPrincipal(member),
                EMPTY_CREDENTIALS,
                Collections.singleton(new SimpleGrantedAuthority(member.getRole()))
        );

        return createToken(authentication);
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
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        return Jwts.builder()
                .claim(ID_KEY, principal.getId())
                .claim(EMAIL_KEY, principal.getUsername())
                .claim(AUTHORITIES_KEY, joinedAuthorities(authentication))
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
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        return Jwts.builder()
                .claim(EMAIL_KEY, principal.getUsername())
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
        MemberPrincipal principal = MemberPrincipal.of(
                payload.get(ID_KEY, Long.class),
                payload.get(EMAIL_KEY, String.class),
                authorities
        );
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
    public String parseEmailFromRefreshToken(String refreshToken) {
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
            throw JWTException.unSupportedToken();
        } catch (JwtException | IllegalArgumentException exception) {
            throw JWTException.invalidToken();
        }
        return payload;
    }
}
