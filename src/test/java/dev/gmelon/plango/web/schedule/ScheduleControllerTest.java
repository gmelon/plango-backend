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
import java.time.LocalTime;
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
                .nickname("nameA")
                .build();
        loginCookieOfMemberA = TestAuthUtil.signupAndGetCookie(memberASignupRequest);

        SignupRequestDto memberBSignupRequest = SignupRequestDto.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .build();
        loginCookieOfMemberB = TestAuthUtil.signupAndGetCookie(memberBSignupRequest);

        memberA = memberRepository.findByEmail(memberASignupRequest.getEmail()).get();
        memberB = memberRepository.findByEmail(memberBSignupRequest.getEmail()).get();
    }

    @Test
    void 일정_생성() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .latitude(36.3674097)
                .longitude(127.3454477)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 공과대학 5호관")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        Long createdScheduleId = parseScheduleIdFrom(response.header(HttpHeaders.LOCATION));

        Schedule createdSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(createdSchedule.getTitle()).isEqualTo(request.getTitle());
        assertThat(createdSchedule.getContent()).isEqualTo(request.getContent());
        assertThat(createdSchedule.getDate()).isEqualTo(request.getDate());
        assertThat(createdSchedule.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(createdSchedule.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(createdSchedule.getLatitude()).isEqualTo(request.getLatitude());
        assertThat(createdSchedule.getLongitude()).isEqualTo(request.getLongitude());
        assertThat(createdSchedule.getRoadAddress()).isEqualTo(request.getRoadAddress());
        assertThat(createdSchedule.getPlaceName()).isEqualTo(request.getPlaceName());
        assertThat(createdSchedule.isDone()).isFalse();
    }

    @Test
    void 일정_생성_시_종료_시각은_시작_시각보다_뒤여야_함() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(9, 59, 59))
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 일정_수정_시_종료_시각은_시작_시각보다_뒤여야_함() {
        // given
        ScheduleEditRequestDto request = ScheduleEditRequestDto.builder()
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(9, 59, 59))
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().patch("/api/schedules/1")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 기록이_없는_일정_단건_조회() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .latitude(36.3674097)
                .longitude(127.3454477)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 공과대학 5호관")
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
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
        assertThat(responseDto.getDate()).isEqualTo(request.getDate());
        assertThat(responseDto.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(responseDto.getLatitude()).isEqualTo(request.getLatitude());
        assertThat(responseDto.getLongitude()).isEqualTo(request.getLongitude());
        assertThat(responseDto.getRoadAddress()).isEqualTo(request.getRoadAddress());
        assertThat(responseDto.getPlaceName()).isEqualTo(request.getPlaceName());
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isFalse();
        assertThat(responseDto.getDiary()).isNull();
    }

    @Test
    void 기록이_있는_일정_단건_조회() {
        // given
        ScheduleCreateRequestDto scheduleRequest = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .latitude(36.3674097)
                .longitude(127.3454477)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 공과대학 5호관")
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(scheduleRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
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
        assertThat(responseDto.getDate()).isEqualTo(scheduleRequest.getDate());
        assertThat(responseDto.getStartTime()).isEqualTo(scheduleRequest.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(scheduleRequest.getEndTime());
        assertThat(responseDto.getLatitude()).isEqualTo(scheduleRequest.getLatitude());
        assertThat(responseDto.getLongitude()).isEqualTo(scheduleRequest.getLongitude());
        assertThat(responseDto.getRoadAddress()).isEqualTo(scheduleRequest.getRoadAddress());
        assertThat(responseDto.getPlaceName()).isEqualTo(scheduleRequest.getPlaceName());
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isTrue();
        assertThat(responseDto.getDiary().getId()).isNotNull();
    }

    @Test
    void 존재하지_않는_일정_단건_조회() {
        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/schedules/1")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("존재하지 않는 일정입니다.");
    }

    @Test
    void 타인의_일정_단건_조회() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberB)
                .when().get(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 일정_수정() {
        // given
        ScheduleCreateRequestDto createRequest = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .latitude(36.3674097)
                .longitude(127.3454477)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 공과대학 5호관")
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdScheduleId = parseScheduleIdFrom(createdScheduleLocation);

        ScheduleEditRequestDto editRequet = ScheduleEditRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 본문")
                .date(LocalDate.of(2024, 7, 27))
                .startTime(LocalTime.of(11, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .latitude(36.3682999)
                .longitude(127.3420364)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 인문대학")
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
        assertThat(foundSchedule.getDate()).isEqualTo(editRequet.getDate());
        assertThat(foundSchedule.getStartTime()).isEqualTo(editRequet.getStartTime());
        assertThat(foundSchedule.getEndTime()).isEqualTo(editRequet.getEndTime());
        assertThat(foundSchedule.getLatitude()).isEqualTo(editRequet.getLatitude());
        assertThat(foundSchedule.getLongitude()).isEqualTo(editRequet.getLongitude());
        assertThat(foundSchedule.getRoadAddress()).isEqualTo(editRequet.getRoadAddress());
        assertThat(foundSchedule.getPlaceName()).isEqualTo(editRequet.getPlaceName());
    }

    @Test
    void 타인의_일정_수정() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);

        ScheduleEditRequestDto editRequet = ScheduleEditRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 본문")
                .date(LocalDate.of(2024, 7, 27))
                .startTime(LocalTime.of(11, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
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
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @ParameterizedTest
    @CsvSource(value = {"false:true", "true:false", "false:false", "true:true"}, delimiter = ':')
    void 일정_완료_여부_변경(boolean given, boolean expected) {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
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
                .when().patch("/api/schedules/" + schedule.getId() + "/done")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(schedule.getId()).get());
        assertThat(foundSchedule.isDone()).isEqualTo(expected);
    }

    @Test
    void 타인의_일정_완료_여부_변경() {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
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
                .when().patch("/api/schedules/" + schedule.getId() + "/done")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 일정_삭제() {
        // given
        ScheduleCreateRequestDto createRequest = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
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
    void 타인의_일정_삭제() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdScheduleId = parseScheduleIdFrom(createdScheduleLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberB)
                .when().delete(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(scheduleRepository.findById(createdScheduleId)).isPresent();
    }

    @Test
    void 날짜별_기록이_없는_일정_목록_조회() {
        // given
        // memberA 일정 추가
        List<Schedule> memberARequests = List.of(
                Schedule.builder()
                        .title("일정 1")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 1))
                        .member(memberA)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("일정 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("일정 5")
                        .date(LocalDate.of(2023, 6, 26))
                        .member(memberA)
                        .build()
        );
        scheduleRepository.saveAll(memberARequests);

        // memberB 일정 추가
        List<Schedule> memberBRequests = List.of(
                Schedule.builder()
                        .title("일정 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("일정 7")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 1))
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
                .when().get("/api/schedules")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedScheduleTitles = List.of("일정 5", "일정 2");

        assertThat(response.jsonPath().getInt("$.size()")).isEqualTo(expectedScheduleTitles.size());
        for (int i = 0; i < response.jsonPath().getInt("$.size()"); i++) {
            assertThat(response.jsonPath().getString("[" + i + "].title"))
                    .isEqualTo(expectedScheduleTitles.get(i));
            assertThat(response.jsonPath().getString("[" + i + "].date")).contains("2023-06-26");
        }
    }

    @Test
    void 날짜별_전체_일정_목록_조회() {
        // given
        // memberA 일정 추가
        List<Schedule> memberARequests = List.of(
                Schedule.builder()
                        .title("일정 1")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("일정 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .member(memberA)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("일정 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 5")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .build()
        );
        scheduleRepository.saveAll(memberARequests);

        // memberB 일정 추가
        List<Schedule> memberBRequests = List.of(
                Schedule.builder()
                        .title("일정 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().title("").build())
                        .build(),
                Schedule.builder()
                        .title("일정 7")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 1))
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
                .when().get("/api/schedules")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedScheduleTitles = List.of("일정 3", "일정 4", "일정 2", "일정 5");

        assertThat(response.jsonPath().getInt("$.size()")).isEqualTo(expectedScheduleTitles.size());
        for (int i = 0; i < response.jsonPath().getInt("$.size()"); i++) {
            assertThat(response.jsonPath().getString("[" + i + "].title"))
                    .isEqualTo(expectedScheduleTitles.get(i));
            assertThat(response.jsonPath().getString("[" + i + "].date")).contains("2023-06-26");
        }
    }

    @Test
    void 월별로_일정이_존재하는_날짜의_목록_조회() {
        // given
        List<Schedule> requests = List.of(
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 5, 31))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 15))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 17))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 30))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 7, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .member(memberA)
                        .build()
        );
        scheduleRepository.saveAll(requests);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("yearMonth", "2023-06").log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/schedules")
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
