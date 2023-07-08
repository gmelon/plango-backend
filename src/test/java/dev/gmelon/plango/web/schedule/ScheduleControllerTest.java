package dev.gmelon.plango.web.schedule;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryCreateRequestDto;
import dev.gmelon.plango.service.schedule.dto.*;
import dev.gmelon.plango.web.TestAuthUtil;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScheduleControllerTest {

    private Cookie loginCookieOfMemberA;
    private Cookie loginCookieOfMemberB;

    private Member memberA;
    private Member memberB;

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

        SignupRequestDto memberBSignupRequest = SignupRequestDto.builder()
                .email("b@b.com")
                .password("passwordB")
                .name("nameB")
                .build();
        loginCookieOfMemberB = TestAuthUtil.signupAndGetCookie(memberBSignupRequest);

        memberA = memberRepository.findByEmail(memberASignupRequest.getEmail()).get();
        memberB = memberRepository.findByEmail(memberBSignupRequest.getEmail()).get();
    }

    @Test
    void 계획_생성() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .location("계획 장소")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        Long createdScheduleId = parseScheduleIdFrom(response.header(HttpHeaders.LOCATION));

        Schedule createdSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(createdSchedule.getTitle()).isEqualTo(request.getTitle());
        assertThat(createdSchedule.getContent()).isEqualTo(request.getContent());
        assertThat(createdSchedule.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(createdSchedule.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(createdSchedule.getLocation()).isEqualTo(request.getLocation());
        assertThat(createdSchedule.isDone()).isFalse();
    }

    @Test
    void 기록이_없는_계획_단건_조회() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .location("계획 장소")
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberA)
                .when().get(createdScheduleLocation)
                .then().log().all().extract();

        // then
        ScheduleResponseDto responseDto = response.as(ScheduleResponseDto.class);
        assertThat(responseDto.getId()).isEqualTo(parseScheduleIdFrom(createdScheduleLocation));
        assertThat(responseDto.getTitle()).isEqualTo(request.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(request.getContent());
        assertThat(responseDto.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(responseDto.getLocation()).isEqualTo(request.getLocation());
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isFalse();
        assertThat(responseDto.getDiary()).isNull();
    }

    @Test
    void 기록이_있는_계획_단건_조회() {
        // given
        ScheduleCreateRequestDto scheduleRequest = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .location("계획 장소")
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(scheduleRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);

        DiaryCreateRequestDto diaryRequest = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .build();
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(diaryRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post(createdScheduleLocation + "/diary")
                .then().log().all();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberA)
                .when().get(createdScheduleLocation)
                .then().log().all().extract();

        // then
        ScheduleResponseDto responseDto = response.as(ScheduleResponseDto.class);
        assertThat(responseDto.getId()).isEqualTo(parseScheduleIdFrom(createdScheduleLocation));
        assertThat(responseDto.getTitle()).isEqualTo(scheduleRequest.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(scheduleRequest.getContent());
        assertThat(responseDto.getStartTime()).isEqualTo(scheduleRequest.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(scheduleRequest.getEndTime());
        assertThat(responseDto.getLocation()).isEqualTo(scheduleRequest.getLocation());
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isTrue();
        assertThat(responseDto.getDiary().getId()).isNotNull();
    }

    @Test
    void 존재하지_않는_계획_단건_조회() {
        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/v1/schedules/1")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("존재하지 않는 계획입니다.");
    }

    @Test
    void 타인의_계획_단건_조회() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberB)
                .when().get(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("권한이 없는 자원입니다.");
    }

    @Test
    void 계획_수정() {
        // given
        ScheduleCreateRequestDto createRequest = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .location("계획 장소")
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdScheduleId = parseScheduleIdFrom(createdScheduleLocation);

        ScheduleEditRequestDto editRequet = ScheduleEditRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 본문")
                .startTime(LocalDateTime.of(2024, 7, 27, 11, 0, 0))
                .endTime(LocalDateTime.of(2024, 7, 27, 12, 0, 0))
                .location("수정된 장소")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(editRequet).log().all()
                .cookie(loginCookieOfMemberA)
                .when().patch(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(foundSchedule.getTitle()).isEqualTo(editRequet.getTitle());
        assertThat(foundSchedule.getContent()).isEqualTo(editRequet.getContent());
        assertThat(foundSchedule.getStartTime()).isEqualTo(editRequet.getStartTime());
        assertThat(foundSchedule.getEndTime()).isEqualTo(editRequet.getEndTime());
        assertThat(foundSchedule.getLocation()).isEqualTo(editRequet.getLocation());
    }

    @Test
    void 타인의_계획_수정() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);

        ScheduleEditRequestDto editRequet = ScheduleEditRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 본문")
                .startTime(LocalDateTime.of(2024, 7, 27, 11, 0, 0))
                .endTime(LocalDateTime.of(2024, 7, 27, 12, 0, 0))
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(editRequet).log().all()
                .cookie(loginCookieOfMemberB)
                .when().patch(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("권한이 없는 자원입니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"false:true", "true:false", "false:false", "true:true"}, delimiter = ':')
    void 계획_완료_여부_변경(boolean given, boolean expected) {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .done(given)
                .member(memberA)
                .build();
        scheduleRepository.save(schedule);

        ScheduleEditDoneRequestDto request = new ScheduleEditDoneRequestDto(expected);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().patch("/api/v1/schedules/" + schedule.getId() + "/done")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(schedule.getId()).get());
        assertThat(foundSchedule.isDone()).isEqualTo(expected);
    }

    @Test
    void 타인의_계획_완료_여부_변경() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .done(false)
                .member(memberA)
                .build();
        scheduleRepository.save(schedule);

        ScheduleEditDoneRequestDto request = new ScheduleEditDoneRequestDto(true);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberB)
                .when().patch("/api/v1/schedules/" + schedule.getId() + "/done")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 계획_삭제() {
        // given
        ScheduleCreateRequestDto createRequest = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdScheduleId = parseScheduleIdFrom(createdScheduleLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().delete(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(scheduleRepository.findById(createdScheduleId)).isEmpty();
    }

    @Test
    void 타인의_계획_삭제() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdScheduleId = parseScheduleIdFrom(createdScheduleLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberB)
                .when().delete(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("권한이 없는 자원입니다.");
        assertThat(scheduleRepository.findById(createdScheduleId)).isPresent();
    }

    @Test
    void 날짜별_기록이_없는_계획_목록_조회() {
        // given
        // memberA 계획 추가
        List<Schedule> memberARequests = List.of(
                Schedule.builder()
                        .title("계획 1")
                        .startTime(LocalDateTime.of(2023, 6, 25, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 2")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 3")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 1))
                        .member(memberA)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("계획 4")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 12, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("계획 5")
                        .startTime(LocalDateTime.of(2023, 6, 26, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 27, 0, 0, 0))
                        .member(memberA)
                        .build()
        );
        scheduleRepository.saveAll(memberARequests);

        // memberB 계획 추가
        List<Schedule> memberBRequests = List.of(
                Schedule.builder()
                        .title("계획 6")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("계획 7")
                        .startTime(LocalDateTime.of(2023, 6, 26, 15, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 22, 0, 1))
                        .member(memberB)
                        .build()
        );
        scheduleRepository.saveAll(memberBRequests);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("date", "2023-06-26")
                .param("noDiaryOnly", true)
                .log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/v1/schedules")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedScheduleTitles = List.of("계획 2", "계획 5");

        assertThat(response.jsonPath().getInt("$.size()")).isEqualTo(expectedScheduleTitles.size());
        for (int i = 0; i < response.jsonPath().getInt("$.size()"); i++) {
            assertThat(response.jsonPath().getString("[" + i + "].title"))
                    .isEqualTo(expectedScheduleTitles.get(i));
            assertThat(response.jsonPath().getString("[" + i + "].startTime")).contains("2023-06-26");
        }
    }

    @Test
    void 날짜별_전체_계획_목록_조회() {
        // given
        // memberA 계획 추가
        List<Schedule> memberARequests = List.of(
                Schedule.builder()
                        .title("계획 1")
                        .startTime(LocalDateTime.of(2023, 6, 25, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("계획 2")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 3")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 1))
                        .member(memberA)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("계획 4")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 5")
                        .startTime(LocalDateTime.of(2023, 6, 26, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 27, 0, 0, 0))
                        .member(memberA)
                        .build()
        );
        scheduleRepository.saveAll(memberARequests);
        scheduleRepository.saveAll(memberARequests);

        // memberB 계획 추가
        List<Schedule> memberBRequests = List.of(
                Schedule.builder()
                        .title("계획 6")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("계획 7")
                        .startTime(LocalDateTime.of(2023, 6, 26, 15, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 22, 0, 1))
                        .member(memberB)
                        .build()
        );
        scheduleRepository.saveAll(memberBRequests);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("date", "2023-06-26")
                .log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/v1/schedules")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedScheduleTitles = List.of("계획 2", "계획 3", "계획 4", "계획 5");

        assertThat(response.jsonPath().getInt("$.size()")).isEqualTo(expectedScheduleTitles.size());
        for (int i = 0; i < response.jsonPath().getInt("$.size()"); i++) {
            assertThat(response.jsonPath().getString("[" + i + "].title"))
                    .isEqualTo(expectedScheduleTitles.get(i));
            assertThat(response.jsonPath().getString("[" + i + "].startTime")).contains("2023-06-26");
        }
    }

    @Test
    void 월별로_계획이_존재하는_날짜의_목록_조회() {
        // given
        List<ScheduleCreateRequestDto> requests = List.of(
                ScheduleCreateRequestDto.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 5, 31, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 1, 12, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 1, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 1, 12, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 1, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 1, 12, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 15, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 17, 12, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 17, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 17, 12, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 30, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 7, 1, 12, 0, 0))
                        .build()
        );
        for (ScheduleCreateRequestDto request : requests) {
            RestAssured
                    .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(request).log().all()
                    .cookie(loginCookieOfMemberA)
                    .when().post("/api/v1/schedules")
                    .then().log().all();
        }

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("yearMonth", "2023-06").log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/v1/schedules")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<ScheduleCountResponseDto> expected = List.of(
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 1), 2),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 15), 1),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 17), 1),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 30), 1)
        );
        for (int i = 0; i < response.jsonPath().getInt("$.size()"); i++) {
            assertThat(response.jsonPath().getString("[" + i + "].date"))
                    .isEqualTo(expected.get(i).getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            assertThat(response.jsonPath().getInt("[" + i + "].count"))
                    .isEqualTo(expected.get(i).getCount());
        }
    }

    private Long parseScheduleIdFrom(String locationHeader) {
        String[] splitedLocation = locationHeader.split("/");
        return Long.parseLong(splitedLocation[splitedLocation.length - 1]);
    }
}
