package dev.gmelon.plango.web.auth;

import dev.gmelon.plango.config.auth.dto.LoginRequestDto;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private DiaryRepository diaryRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 정상_값으로_회원가입() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .when().post("/api/auth/signup")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(memberRepository.findByEmail(request.getEmail())).isPresent();

        Member member = memberRepository.findByEmail(request.getEmail()).get();
        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getNickname()).isEqualTo(request.getNickname());
    }

    @Test
    void 이미_존재하는_이메일로_회원가입() {
        // given
        SignupRequestDto firstRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(firstRequest).log().all()
                .when().post("/api/auth/signup")
                .then().log().all();

        SignupRequestDto secondRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameB")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(secondRequest).log().all()
                .when().post("/api/auth/signup")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("이미 존재하는 회원입니다.");
    }

    @Test
    void 이미_존재하는_닉네임으로_회원가입() {
        // given
        SignupRequestDto firstRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(firstRequest).log().all()
                .when().post("/api/auth/signup")
                .then().log().all();

        SignupRequestDto secondRequest = SignupRequestDto.builder()
                .email("b@b.com")
                .password("passwordA")
                .nickname("nameA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(secondRequest).log().all()
                .when().post("/api/auth/signup")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("이미 존재하는 회원입니다.");
    }

    @Test
    void 정상_이메일로_로그인() {
        // given
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        TestAuthUtil.signupAndGetCookie(signupRequest);

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("a@a.com")
                .password("passwordA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest).log().all()
                .when().post("/api/auth/login")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.header("Set-Cookie")).isNotBlank();
    }

    @Test
    void 존재하지_않는_이메일로_로그인() {
        // given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("a@a.com")
                .password("passwordA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest).log().all()
                .when().post("/api/auth/login")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 정상_닉네임으로_로그인() {
        // given
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        TestAuthUtil.signupAndGetCookie(signupRequest);

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("nameA")
                .password("passwordA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest).log().all()
                .when().post("/api/auth/login")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.header("Set-Cookie")).isNotBlank();
    }

    @Test
    void 존재하지_않는_닉네임으로_로그인() {
        // given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("nameA")
                .password("passwordA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequest).log().all()
                .when().post("/api/auth/login")
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
                .nickname("nameA")
                .build();
        Cookie loginCookie = TestAuthUtil.signupAndGetCookie(signupRequest);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(loginCookie).log().all()
                .when().post("/api/auth/logout")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.sessionId()).isNull();
    }

    @Test
    void 회원이_스스로_회원_탈퇴() {
        // given
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        Cookie loginCookie = TestAuthUtil.signupAndGetCookie(signupRequest);
        Member member = memberRepository.findByEmail(signupRequest.getEmail()).get();

        Diary diary = Diary.builder()
                .title("기록 제목")
                .build();
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .diary(diary)
                .member(member)
                .build();
        scheduleRepository.save(schedule);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(loginCookie).log().all()
                .when().delete("/api/auth/signout")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(scheduleRepository.findAllByMemberId(member.getId())).hasSize(0);
        assertThat(diaryRepository.findByTitle(diary.getTitle())).isEmpty();
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }

    @Test
    void 관리자를_통한_회원_탈퇴() {
        // given
        Member member = Member.builder()
                .email("a@a.com")
                .nickname("nameA")
                .password("passwordA")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(member);

        Diary diary = Diary.builder()
                .title("기록 제목")
                .build();
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .diary(diary)
                .member(member)
                .build();
        scheduleRepository.save(schedule);

        Member admin = Member.builder()
                .email("admin@admin.com")
                .nickname("admin")
                .password(passwordEncoder.encode("passwordA"))
                .role(MemberRole.ROLE_ADMIN)
                .build();
        memberRepository.save(admin);
        SignupRequestDto loginRequest = SignupRequestDto.builder()
                .email(admin.getEmail())
                .password("passwordA")
                .build();
        Cookie adminCookie = TestAuthUtil.loginAndGetCookie(loginRequest);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(adminCookie).log().all()
                .when().delete("/api/auth/signout/" + member.getId())
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(scheduleRepository.findAllByMemberId(member.getId())).hasSize(0);
        assertThat(diaryRepository.findByTitle(diary.getTitle())).isEmpty();
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }

    @Test
    void 일반_회원이_다른_회원의_회원_탈퇴_시도() {
        // given
        SignupRequestDto signupRequestA = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        Cookie loginCookieA = TestAuthUtil.signupAndGetCookie(signupRequestA);

        SignupRequestDto signupRequestB = SignupRequestDto.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(loginCookieA).log().all()
                .when().delete("/api/auth/signout/2")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}
