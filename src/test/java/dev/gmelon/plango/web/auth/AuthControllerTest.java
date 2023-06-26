package dev.gmelon.plango.web.auth;

import dev.gmelon.plango.auth.PasswordEncoder;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import dev.gmelon.plango.service.auth.dto.LoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.web.TestAuthUtil;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        memberRepository.deleteAll();
    }

    @Test
    void 정상_값으로_회원가입() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .when().post("/api/v1/auth/signup")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(memberRepository.findByEmail(request.getEmail())).isPresent();

        Member member = memberRepository.findByEmail(request.getEmail()).get();
        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getName()).isEqualTo(request.getName());
    }

    @Test
    void 이미_존재하는_이메일로_회원가입() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .build();
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .when().post("/api/v1/auth/signup")
                .then().log().all();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .when().post("/api/v1/auth/signup")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("이미 존재하는 회원입니다.");
    }

    @Test
    void 정상_값으로_로그인() {
        // given
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .build();
        Cookie loginCookie = TestAuthUtil.signupAndGetCookie(signupRequest);

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest).log().all()
                .when().post("/api/v1/auth/login")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.header("Set-Cookie")).isNotBlank();
    }

    @Test
    void 존재하지_않는_회원으로_로그인() {
        // given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest).log().all()
                .when().post("/api/v1/auth/login")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 로그아웃() {
        // given
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .build();
        Cookie loginCookie = TestAuthUtil.signupAndGetCookie(signupRequest);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(loginCookie).log().all()
                .when().post("/api/v1/auth/logout")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.sessionId()).isNull();
    }

}
