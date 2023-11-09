package dev.gmelon.plango.config.auth.jwt;

import static dev.gmelon.plango.domain.member.MemberRole.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import dev.gmelon.plango.config.auth.dto.MemberPrincipal;
import dev.gmelon.plango.config.auth.dto.TokenResponseDto;
import dev.gmelon.plango.exception.auth.JWTException;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
class JWTProviderTest {
    private static Authentication MEMBER_AUTHENTICATION;

    @BeforeAll
    static void beforeAll() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(ROLE_USER.name()));
        MemberPrincipal principal = MemberPrincipal.of("a@a.com", authorities);

        MEMBER_AUTHENTICATION = UsernamePasswordAuthenticationToken.authenticated(principal, "", authorities);
    }

    @MockBean
    private Clock clock;

    @Autowired
    private JWTProvider jwtProvider;

    @Test
    void 유효한_Access_Token을_파싱하면_Authentication이_생성된다() {
        // given
        mockClockBefore(25);
        TokenResponseDto tokenResponseDto = jwtProvider.createToken(MEMBER_AUTHENTICATION);

        // when
        Authentication parsedAuthentication = jwtProvider.parseAccessToken(tokenResponseDto.getAccessToken());

        // then
        assertThat(parsedAuthentication).usingRecursiveComparison()
                .isEqualTo(MEMBER_AUTHENTICATION);
    }

    @Test
    void 만료일자가_지난_Access_Token을_파싱하면_예외가_발생한다() {
        // given
        mockClockBefore(30);
        TokenResponseDto tokenResponseDto = jwtProvider.createToken(MEMBER_AUTHENTICATION);

        // when, then
        assertThatThrownBy(() -> jwtProvider.parseAccessToken(tokenResponseDto.getAccessToken()))
                .isInstanceOf(JWTException.class);
    }

    @Test
    void 유효한_Refresh_Token을_파싱하면_사용자_email을_반환한다() {
        // given
        mockClockBefore(24 * 60 * 30 - 5);
        TokenResponseDto tokenResponseDto = jwtProvider.createToken(MEMBER_AUTHENTICATION);

        // when
        String parsedMemberEmail = jwtProvider.parseRefreshToken(tokenResponseDto.getRefreshToken());

        // then
        assertThat(parsedMemberEmail).isEqualTo(MEMBER_AUTHENTICATION.getName());
    }

    @Test
    void 만료일자가_지난_Refresh_Token을_파싱하면_예외가_발생한다() {
        // given
        mockClockBefore(24 * 60 * 30);
        TokenResponseDto tokenResponseDto = jwtProvider.createToken(MEMBER_AUTHENTICATION);

        // when, then
        assertThatThrownBy(() -> jwtProvider.parseRefreshToken(tokenResponseDto.getRefreshToken()))
                .isInstanceOf(JWTException.class);
    }

    private void mockClockBefore(long minutes) {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(ZonedDateTime.now().minusMinutes(minutes).toInstant());
    }
}
