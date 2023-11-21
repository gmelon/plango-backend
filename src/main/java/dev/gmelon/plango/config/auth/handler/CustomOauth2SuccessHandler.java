package dev.gmelon.plango.config.auth.handler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpStatus.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.auth.jwt.JWTProvider;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.service.auth.dto.TokenResponseDto;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {
    private final MemberRepository memberRepository;
    private final JWTProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Member member = findMember(authentication);
        TokenResponseDto tokenResponseDto = jwtProvider.createToken(member);

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8.name());
        response.setStatus(SC_OK);

        objectMapper.writeValue(response.getWriter(), tokenResponseDto);
    }

    private Member findMember(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getName();

        return memberRepository.findByEmail(email)
                .orElseThrow(NoSuchMemberException::new);
    }
}
