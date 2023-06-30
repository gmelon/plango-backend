package dev.gmelon.plango.service.auth;

import dev.gmelon.plango.auth.PasswordEncoder;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.service.auth.dto.LoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MemberRepository memberRepository;


    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void 정상_값으로_회원가입() {
        // given
        String unencodedPassword = "passwordA";

        SignupRequestDto request = SignupRequestDto.builder()
                .email("a@a.com")
                .password(unencodedPassword)
                .name("nameA")
                .build();

        // when
        authService.signup(request);

        // then
        Member member = assertDoesNotThrow(() -> memberRepository.findByEmail(request.getEmail()).get());
        assertThat(passwordEncoder.matches(unencodedPassword, member.getPassword())).isTrue();
        assertThat(member.getName()).isEqualTo(request.getName());
    }

    @Test
    void 이미_존재하는_이메일로_회원가입() {
        SignupRequestDto request = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .build();
        authService.signup(request);

        // when, then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 회원입니다.");
    }

    @Test
    void 정상_값으로_로그인() {
        // given
        String unencodedPassword = "passwordA";

        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password(unencodedPassword)
                .name("nameA")
                .build();
        authService.signup(signupRequest);

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email(signupRequest.getEmail())
                .password(unencodedPassword)
                .build();

        // when
        Member loginedMember = authService.login(loginRequest);

        // then
        assertThat(loginedMember.getEmail()).isEqualTo(signupRequest.getEmail());
        assertThat(passwordEncoder.matches(unencodedPassword, loginedMember.getPassword())).isTrue();
        assertThat(loginedMember.getName()).isEqualTo(signupRequest.getName());
    }

    @Test
    void 존재하지_않는_회원으로_로그인() {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .build();

        // when, then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
    }
}
