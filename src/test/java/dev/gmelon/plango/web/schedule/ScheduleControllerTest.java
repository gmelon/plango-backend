package dev.gmelon.plango.web.schedule;

import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleCountResponseDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleCreateRequestDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleEditRequestDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleResponseDto;
import dev.gmelon.plango.web.TestAuthUtil;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScheduleControllerTest {

    private Cookie memberALoginCookie;
    private Cookie memberBLoginCookie;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @LocalServerPort
    private int port;

    @BeforeAll
    void beforeAll() {
        RestAssured.port = port;

        SignupRequestDto memberASignupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .build();
        memberALoginCookie = TestAuthUtil.signupAndGetCookie(memberASignupRequest);

        SignupRequestDto memberBSignupRequest = SignupRequestDto.builder()
                .email("b@b.com")
                .password("passwordB")
                .name("nameB")
                .build();
        memberBLoginCookie = TestAuthUtil.signupAndGetCookie(memberBSignupRequest);
    }

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
    }

    @Test
    void 계획_생성() {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(memberALoginCookie)
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
    }

    @Test
    void 계획_단건_조회() {
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
                .cookie(memberALoginCookie)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(memberALoginCookie)
                .when().get(createdScheduleLocation)
                .then().log().all().extract();

        // then
        ScheduleResponseDto responseDto = response.as(ScheduleResponseDto.class);
        assertThat(responseDto.getTitle()).isEqualTo(request.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(request.getContent());
        assertThat(responseDto.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(request.getEndTime());
    }

    @Test
    void 존재하지_않는_계획_단건_조회() {
        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(memberALoginCookie)
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
                .cookie(memberALoginCookie)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(memberBLoginCookie)
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
                .build();
        String createdScheduleLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(memberALoginCookie)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdScheduleId = parseScheduleIdFrom(createdScheduleLocation);

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
                .cookie(memberALoginCookie)
                .when().put(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(foundSchedule.getTitle()).isEqualTo(editRequet.getTitle());
        assertThat(foundSchedule.getContent()).isEqualTo(editRequet.getContent());
        assertThat(foundSchedule.getStartTime()).isEqualTo(editRequet.getStartTime());
        assertThat(foundSchedule.getEndTime()).isEqualTo(editRequet.getEndTime());
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
                .cookie(memberALoginCookie)
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
                .cookie(memberBLoginCookie)
                .when().put(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("권한이 없는 자원입니다.");
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
                .cookie(memberALoginCookie)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdScheduleId = parseScheduleIdFrom(createdScheduleLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(memberALoginCookie)
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
                .cookie(memberALoginCookie)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdScheduleId = parseScheduleIdFrom(createdScheduleLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(memberBLoginCookie)
                .when().delete(createdScheduleLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.as(ErrorResponseDto.class).getMessage()).isEqualTo("권한이 없는 자원입니다.");
        assertThat(scheduleRepository.findById(createdScheduleId)).isPresent();
    }

    @Test
    void 날짜별_계획_목록_조회() {
        // given
        // memberA 계획 추가
        List<ScheduleCreateRequestDto> memberARequests = List.of(
                ScheduleCreateRequestDto.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 1))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 25, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 27, 0, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 12, 0, 0))
                        .build()
        );
        for (ScheduleCreateRequestDto memberARequest : memberARequests) {
            RestAssured
                    .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(memberARequest).log().all()
                    .cookie(memberALoginCookie)
                    .when().post("/api/v1/schedules")
                    .then().log().all();
        }

        // memberB 계획 추가
        List<ScheduleCreateRequestDto> memberBRequests = List.of(
                ScheduleCreateRequestDto.builder()
                        .title("B의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                        .build(),
                ScheduleCreateRequestDto.builder()
                        .title("B의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 15, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 22, 0, 1))
                        .build()
        );
        for (ScheduleCreateRequestDto memberARequest : memberARequests) {
            RestAssured
                    .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(memberARequest).log().all()
                    .cookie(memberBLoginCookie)
                    .when().post("/api/v1/schedules")
                    .then().log().all();
        }

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("requestDate", "2023-06-26").log().all()
                .cookie(memberALoginCookie)
                .when().get("/api/v1/schedules/day")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        for (int i = 0; i < response.jsonPath().getInt("$.size()"); i++) {
            assertThat(response.jsonPath().getString("[" + i + "].title")).isNotEqualTo("B의 계획");
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
                    .cookie(memberALoginCookie)
                    .when().post("/api/v1/schedules")
                    .then().log().all();
        }

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("requestMonth", "2023-06").log().all()
                .cookie(memberALoginCookie)
                .when().get("/api/v1/schedules/month")
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
