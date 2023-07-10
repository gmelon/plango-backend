package dev.gmelon.plango.web.member;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberStatisticsResponseDto;
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
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerTest {

    private Cookie loginCookieOfMemberA;
    private Member memberA;

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        SignupRequestDto memberASignupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .build();
        loginCookieOfMemberA = TestAuthUtil.signupAndGetCookie(memberASignupRequest);

        memberA = memberRepository.findByEmail(memberASignupRequest.getEmail()).get();
    }

    @Test
    void 나의_프로필_조회() {
        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/v1/members/profile")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        MemberProfileResponseDto responseDto = response.as(MemberProfileResponseDto.class);
        assertThat(responseDto.getEmail()).isEqualTo(memberA.getEmail());
        assertThat(responseDto.getName()).isEqualTo(memberA.getName());
    }

    @Test
    void 나의_통계정보_조회() {
        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/v1/members/statistics")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        MemberStatisticsResponseDto responseDto = response.as(MemberStatisticsResponseDto.class);
        assertThat(responseDto.getScheduleCount()).isGreaterThanOrEqualTo(0);
        assertThat(responseDto.getDoneScheduleCount())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(responseDto.getScheduleCount());
        assertThat(responseDto.getDiaryCount()).isGreaterThanOrEqualTo(0);
    }
}
