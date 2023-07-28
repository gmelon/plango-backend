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

import java.time.LocalDate;
import java.time.LocalTime;
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

    private Schedule scheduleOfMemberA;

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

        scheduleOfMemberA = Schedule.builder()
                .title("계획 제목")
                .content("계획 본문")
                .date(LocalDate.of(2023, 6, 25))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .placeName("계획 장소")
                .member(memberA)
                .build();
        scheduleRepository.save(scheduleOfMemberA);
    }

    @Test
    void 기록_생성_시_제목_또는_사진은_필수_값() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .content("기록 본문")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 기록_수정_시_제목_또는_사진은_필수_값() {
        // given
        DiaryEditRequestDto request = DiaryEditRequestDto.builder()
                .content("기록 본문")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().put("/api/diaries/1")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 자신의_계획에_기록_생성() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
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
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberB)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 기록_단건_조회() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
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
        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("id", "schedule")
                .isEqualTo(request);

        assertThat(responseDto.getSchedule().getId()).isEqualTo(scheduleOfMemberA.getId());
        assertThat(responseDto.getSchedule().getTitle()).isEqualTo(scheduleOfMemberA.getTitle());
        assertThat(responseDto.getSchedule().getDate()).isEqualTo(scheduleOfMemberA.getDate());
        assertThat(responseDto.getSchedule().getStartTime()).isEqualTo(scheduleOfMemberA.getStartTime());
        assertThat(responseDto.getSchedule().getEndTime()).isEqualTo(scheduleOfMemberA.getEndTime());
        assertThat(responseDto.getSchedule().getPlaceName()).isEqualTo(scheduleOfMemberA.getPlaceName());
    }

    @Test
    void 타인의_기록_단건_조회() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberB)
                .when().get(createdDiaryLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 존재하지_않는_기록_단건_조회() {
        // given
        String requestUrl = "/api/diaries/1";

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().get(requestUrl)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 계획_id로_기록_단건_조회() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdDiaryId = parseIdFrom(createdDiaryLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        DiaryResponseDto responseDto = response.as(DiaryResponseDto.class);
        assertThat(responseDto.getId()).isEqualTo(createdDiaryId);
        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("id", "schedule")
                .isEqualTo(request);

        assertThat(responseDto.getSchedule().getId()).isEqualTo(scheduleOfMemberA.getId());
        assertThat(responseDto.getSchedule().getTitle()).isEqualTo(scheduleOfMemberA.getTitle());
        assertThat(responseDto.getSchedule().getDate()).isEqualTo(scheduleOfMemberA.getDate());
        assertThat(responseDto.getSchedule().getStartTime()).isEqualTo(scheduleOfMemberA.getStartTime());
        assertThat(responseDto.getSchedule().getEndTime()).isEqualTo(scheduleOfMemberA.getEndTime());
    }

    @Test
    void 타인의_계획_id로_기록_단건_조회() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberB)
                .when().get("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 존재하지_않는_계획_id로_기록_단건_조회() {
        // given
        String requestUrl = "/api/schedules/" + (scheduleOfMemberA.getId() + 1) + "/diary";

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberA)
                .when().get(requestUrl)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 기록_수정() {
        // given
        DiaryCreateRequestDto createRequest = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdDiaryId = parseIdFrom(createdDiaryLocation);

        DiaryEditRequestDto editRequest = DiaryEditRequestDto.builder()
                .title("기록 제목 2")
                .content("기록 본문 2")
                .imageUrl("https://plango-backend/imageB.jpg")
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
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);

        DiaryEditRequestDto editRequest = DiaryEditRequestDto.builder()
                .title("기록 제목 2")
                .content("기록 본문 2")
                .imageUrl("https://plango-backend/imageB.jpg")
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
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 기록_삭제() {
        // given
        DiaryCreateRequestDto createRequest = DiaryCreateRequestDto.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
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
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        String createdDiaryLocation = RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createRequest).log().all()
                .cookie(loginCookieOfMemberA)
                .when().post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                .thenReturn().header(HttpHeaders.LOCATION);
        Long createdDiaryId = parseIdFrom(createdDiaryLocation);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .cookie(loginCookieOfMemberB)
                .when().delete(createdDiaryLocation)
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(diaryRepository.findById(createdDiaryId)).isPresent();
    }

    @Test
    void 날짜별_기록_목록_조회() {
        // given
        List<Schedule> schedules = List.of(
                Schedule.builder()
                        .title("계획 1")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 1").build())
                        .build(),
                Schedule.builder()
                        .title("계획 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 2").build())
                        .build(),
                Schedule.builder()
                        .title("계획 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 1))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 3").build())
                        .build(),
                Schedule.builder()
                        .title("계획 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 4").build())
                        .build(),
                Schedule.builder()
                        .title("계획 5")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().title("기록 5").build())
                        .build(),
                Schedule.builder()
                        .title("계획 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 7")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().title("기록 7").build())
                        .build(),
                Schedule.builder()
                        .title("계획 8")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().title("기록 8").build())
                        .build()
        );
        scheduleRepository.saveAll(schedules);

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("date", "2023-06-26").log().all()
                .cookie(loginCookieOfMemberA)
                .when().get("/api/diaries")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Integer> expectedTitleIndex = List.of(2, 3, 4, 5);
        for (int i = 0; i < response.jsonPath().getInt("$.size()"); i++) {
            assertThat(response.jsonPath().getString("[" + i + "].title"))
                    .isEqualTo("기록 " + expectedTitleIndex.get(i));
            assertThat(response.jsonPath().getString("[" + i + "].schedule.title"))
                    .isEqualTo("계획 " + expectedTitleIndex.get(i));
        }
    }

    private Long parseIdFrom(String locationHeader) {
        String[] splitedLocation = locationHeader.split("/");
        return Long.parseLong(splitedLocation[splitedLocation.length - 1]);
    }
}
