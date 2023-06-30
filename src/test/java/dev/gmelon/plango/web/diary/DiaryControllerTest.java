package dev.gmelon.plango.web.diary;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryCreateRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryEditRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryResponseDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleCreateRequestDto;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiaryControllerTest {

    private Cookie loginCookieOfMemberA;
    private Cookie loginCookieOfMemberB;

    private Member memberA;
    private Member memberB;

    private Long scheduleIdOfMemberA;

    @Autowired
    private DiaryRepository diaryRepository;
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

        ScheduleCreateRequestDto scheduleCreateRequestDto = ScheduleCreateRequestDto.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 25, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 25, 11, 0, 0))
                .build();
        String locationHeader = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(scheduleCreateRequestDto).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules")
                .thenReturn().header(HttpHeaders.LOCATION);
        scheduleIdOfMemberA = parseIdFrom(locationHeader);
    }

    @Test
    void 자신의_계획에_기록_생성() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules/" + scheduleIdOfMemberA + "/diary")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        Long createdDiaryId = parseIdFrom(response.header(HttpHeaders.LOCATION));
        Diary createdDiary = assertDoesNotThrow(() -> diaryRepository.findById(createdDiaryId).get());
        assertThat(createdDiary.getTitle()).isEqualTo(request.getTitle());
        assertThat(createdDiary.getContent()).isEqualTo(request.getContent());
        assertThat(createdDiary.getImageUrl()).isEqualTo(request.getImageUrl());
    }

    @Test
    void 타인의_계획에_기록_생성() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberB)
                .when().post("/api/v1/schedules/" + scheduleIdOfMemberA + "/diary")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 기록_단건_조회() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules/" + scheduleIdOfMemberA + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().get(createdDiaryLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        DiaryResponseDto responseDto = response.as(DiaryResponseDto.class);
        assertThat(responseDto.getId()).isEqualTo(parseIdFrom(createdDiaryLocation));
        assertThat(responseDto.getTitle()).isEqualTo(request.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(request.getContent());
        assertThat(responseDto.getImageUrl()).isEqualTo(request.getImageUrl());
    }

    @Test
    void 타인의_기록_단건_조회() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules/" + scheduleIdOfMemberA + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberB)
                .when().get(createdDiaryLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 존재하지_않는_기록_단건_조회() {
        // given
        String requestUrl = "/api/v1/diaries/1";

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().get(requestUrl)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 기록_수정() {
        // given
        DiaryCreateRequestDto createRequest = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules/" + scheduleIdOfMemberA + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdDiaryId = parseIdFrom(createdDiaryLocation);

        DiaryEditRequestDto editRequest = DiaryEditRequestDto.builder()
                .title("기록 제목 2")
                .content("기록 본문 2")
                .imageUrl("https://image.com/imageB")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(editRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().put(createdDiaryLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        Diary createdDiary = assertDoesNotThrow(() -> diaryRepository.findById(createdDiaryId).get());
        assertThat(createdDiary.getTitle()).isEqualTo(editRequest.getTitle());
        assertThat(createdDiary.getContent()).isEqualTo(editRequest.getContent());
        assertThat(createdDiary.getImageUrl()).isEqualTo(editRequest.getImageUrl());
    }

    @Test
    void 타인의_기록_수정() {
        // given
        DiaryCreateRequestDto createRequest = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules/" + scheduleIdOfMemberA + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);

        DiaryEditRequestDto editRequest = DiaryEditRequestDto.builder()
                .title("기록 제목 2")
                .content("기록 본문 2")
                .imageUrl("https://image.com/imageB")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(editRequest).log().all()
                .cookie(loginCookieOfMemberB)
                .when().put(createdDiaryLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 기록_삭제() {
        // given
        DiaryCreateRequestDto createRequest = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules/" + scheduleIdOfMemberA + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdDiaryId = parseIdFrom(createdDiaryLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().delete(createdDiaryLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(diaryRepository.findById(createdDiaryId)).isEmpty();
    }

    @Test
    void 타인의_기록_삭제() {
        // given
        DiaryCreateRequestDto createRequest = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/v1/schedules/" + scheduleIdOfMemberA + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdDiaryId = parseIdFrom(createdDiaryLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberB)
                .when().delete(createdDiaryLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(diaryRepository.findById(createdDiaryId)).isPresent();
    }

    @Test
    void 날짜별_기록_목록_조회() {
        // given
        List<Schedule> schedules = List.of(
                Schedule.builder()
                        .title("계획 1")
                        .startTime(LocalDateTime.of(2023, 6, 25, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 1").build())
                        .build(),
                Schedule.builder()
                        .title("계획 2")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 2").build())
                        .build(),
                Schedule.builder()
                        .title("계획 3")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 1))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 3").build())
                        .build(),
                Schedule.builder()
                        .title("계획 4")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 12, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 4").build())
                        .build(),
                Schedule.builder()
                        .title("계획 5")
                        .startTime(LocalDateTime.of(2023, 6, 26, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 27, 0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 5").build())
                        .build(),
                Schedule.builder()
                        .title("계획 6")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().title("기록 6").build())
                        .build(),
                Schedule.builder()
                        .title("계획 7")
                        .startTime(LocalDateTime.of(2023, 6, 26, 15, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 22, 0, 1))
                        .member(memberB)
                        .diary(Diary.builder().title("기록 7").build())
                        .build()
        );
        scheduleRepository.saveAll(schedules);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("date", "2023-06-26").log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/v1/diaries")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Integer> expectedDiaryId = List.of(2, 3, 4, 5);
        for (int i = 0; i < response.jsonPath().getInt("$.size()"); i++) {
            assertThat(response.jsonPath().getString("[" + i + "].title"))
                    .isEqualTo("기록 " + expectedDiaryId.get(i));
        }
    }

    private Long parseIdFrom(String locationHeader) {
        String[] splitedLocation = locationHeader.split("/");
        return Long.parseLong(splitedLocation[splitedLocation.length - 1]);
    }
}
