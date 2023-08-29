package dev.gmelon.plango.web.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import dev.gmelon.plango.service.schedule.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class ScheduleControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private DiaryRepository diaryRepository;

    @PlangoMockUser
    @Test
    void 일정_생성() throws Exception {
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
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Long createdScheduleId = parseScheduleIdFrom(response.getHeader(HttpHeaders.LOCATION));

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

    @PlangoMockUser
    @Test
    void 일정_생성_시_종료_시각은_시작_시각보다_뒤여야_함() throws Exception {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(9, 59, 59))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 일정_수정_시_종료_시각은_시작_시각보다_뒤여야_함() throws Exception {
        // given
        ScheduleEditRequestDto request = ScheduleEditRequestDto.builder()
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(9, 59, 59))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/schedules/1")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 기록이_없는_일정_단건_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .member(member)
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
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/" + savedSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        ScheduleResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(savedSchedule.getId());
        assertThat(responseDto.getTitle()).isEqualTo(givenSchedule.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(givenSchedule.getContent());
        assertThat(responseDto.getDate()).isEqualTo(givenSchedule.getDate());
        assertThat(responseDto.getStartTime()).isEqualTo(givenSchedule.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(givenSchedule.getEndTime());
        assertThat(responseDto.getLatitude()).isEqualTo(givenSchedule.getLatitude());
        assertThat(responseDto.getLongitude()).isEqualTo(givenSchedule.getLongitude());
        assertThat(responseDto.getRoadAddress()).isEqualTo(givenSchedule.getRoadAddress());
        assertThat(responseDto.getPlaceName()).isEqualTo(givenSchedule.getPlaceName());
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isFalse();
    }

    @PlangoMockUser
    @Test
    void 기록이_있는_일정_단건_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .member(member)
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
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        Diary givenDiary = Diary.builder()
                .content("기록 본문")
                .member(member)
                .schedule(givenSchedule)
                .build();
        diaryRepository.save(givenDiary);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/" + savedSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ScheduleResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(savedSchedule.getId());
        assertThat(responseDto.getTitle()).isEqualTo(savedSchedule.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(savedSchedule.getContent());
        assertThat(responseDto.getDate()).isEqualTo(savedSchedule.getDate());
        assertThat(responseDto.getStartTime()).isEqualTo(savedSchedule.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(savedSchedule.getEndTime());
        assertThat(responseDto.getLatitude()).isEqualTo(savedSchedule.getLatitude());
        assertThat(responseDto.getLongitude()).isEqualTo(savedSchedule.getLongitude());
        assertThat(responseDto.getRoadAddress()).isEqualTo(savedSchedule.getRoadAddress());
        assertThat(responseDto.getPlaceName()).isEqualTo(savedSchedule.getPlaceName());
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isTrue();
    }

    @PlangoMockUser
    @Test
    void 존재하지_않는_일정_단건_조회() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        ErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), ErrorResponseDto.class);
        assertThat(responseDto.getMessage()).isEqualTo("존재하지 않는 일정입니다.");
    }

    @PlangoMockUser
    @Test
    void 타인의_일정_단건_조회() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .member(anotherMember)
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/" + savedSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 일정_수정() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .member(member)
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
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        ScheduleEditRequestDto request = ScheduleEditRequestDto.builder()
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
        MockHttpServletResponse response = mockMvc.perform(patch("/api/schedules/" + savedSchedule.getId())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(savedSchedule.getId()).get());
        assertThat(foundSchedule.getTitle()).isEqualTo(request.getTitle());
        assertThat(foundSchedule.getContent()).isEqualTo(request.getContent());
        assertThat(foundSchedule.getDate()).isEqualTo(request.getDate());
        assertThat(foundSchedule.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(foundSchedule.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(foundSchedule.getLatitude()).isEqualTo(request.getLatitude());
        assertThat(foundSchedule.getLongitude()).isEqualTo(request.getLongitude());
        assertThat(foundSchedule.getRoadAddress()).isEqualTo(request.getRoadAddress());
        assertThat(foundSchedule.getPlaceName()).isEqualTo(request.getPlaceName());
    }

    @PlangoMockUser
    @Test
    void 타인의_일정_수정() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .member(anotherMember)
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        ScheduleEditRequestDto request = ScheduleEditRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 본문")
                .date(LocalDate.of(2024, 7, 27))
                .startTime(LocalTime.of(11, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/schedules/" + savedSchedule.getId())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @ParameterizedTest
    @CsvSource(value = {"false:true", "true:false", "false:false", "true:true"}, delimiter = ':')
    void 일정_완료_여부_변경(boolean given, boolean expected) throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .done(given)
                .member(member)
                .build();
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        ScheduleEditDoneRequestDto request = new ScheduleEditDoneRequestDto(expected);

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/schedules/" + savedSchedule.getId() + "/done")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(givenSchedule.getId()).get());
        assertThat(foundSchedule.isDone()).isEqualTo(expected);
    }

    @PlangoMockUser
    @Test
    void 타인의_일정_완료_여부_변경() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .member(anotherMember)
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .done(false)
                .build();
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        ScheduleEditDoneRequestDto request = new ScheduleEditDoneRequestDto(true);

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/schedules/" + savedSchedule.getId() + "/done")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 일정_삭제() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .member(member)
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + savedSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(scheduleRepository.findById(savedSchedule.getId())).isEmpty();
    }

    @PlangoMockUser
    @Test
    void 타인의_일정_삭제() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .member(anotherMember)
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + savedSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(scheduleRepository.findById(savedSchedule.getId())).isPresent();
    }

    @PlangoMockUser
    @Test
    void 날짜별_기록이_없는_일정_목록_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        List<Schedule> memberAScheduleRequests = List.of(
                Schedule.builder()
                        .member(member)
                        .title("일정 1")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 1))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 5")
                        .date(LocalDate.of(2023, 6, 26))
                        .build()
        );
        scheduleRepository.saveAll(memberAScheduleRequests);

        List<Diary> memberADiaryRequests = List.of(
                Diary.builder()
                        .member(member)
                        .schedule(memberAScheduleRequests.get(2))
                        .content("")
                        .build(),
                Diary.builder()
                        .member(member)
                        .schedule(memberAScheduleRequests.get(3))
                        .content("")
                        .build()
        );
        diaryRepository.saveAll(memberADiaryRequests);

        Member anotherMember = createAnotherMember();
        List<Schedule> memberBScheduleRequests = List.of(
                Schedule.builder()
                        .member(anotherMember)
                        .title("일정 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(anotherMember)
                        .title("일정 7")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 1))
                        .build()
        );
        scheduleRepository.saveAll(memberBScheduleRequests);

        List<Diary> memberBDiaryRequests = List.of(
                Diary.builder()
                        .member(anotherMember)
                        .schedule(memberBScheduleRequests.get(0))
                        .content("")
                        .build()
        );
        diaryRepository.saveAll(memberBDiaryRequests);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/")
                        .param("date", "2023-06-26")
                        .param("noDiaryOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedScheduleTitles = List.of("일정 5", "일정 2");

        ScheduleListResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleListResponseDto[].class);

        assertThat(responseDtos.length).isEqualTo(expectedScheduleTitles.size());
        assertThat(responseDtos)
                .extracting(ScheduleListResponseDto::getTitle)
                .isEqualTo(expectedScheduleTitles);
        assertThat(responseDtos)
                .extracting(ScheduleListResponseDto::getDate)
                .containsOnly(LocalDate.of(2023, 6, 26));
    }

    @PlangoMockUser
    @Test
    void 날짜별_전체_일정_목록_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        List<Schedule> memberAScheduleRequests = List.of(
                Schedule.builder()
                        .member(member)
                        .title("일정 1")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .build(),
                Schedule.builder()
                        .title("일정 5")
                        .member(member)
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build()
        );
        scheduleRepository.saveAll(memberAScheduleRequests);

        List<Diary> memberADiaryRequests = List.of(
                Diary.builder()
                        .member(member)
                        .schedule(memberAScheduleRequests.get(0))
                        .content("")
                        .build(),
                Diary.builder()
                        .member(member)
                        .schedule(memberAScheduleRequests.get(2))
                        .content("")
                        .build()
        );
        diaryRepository.saveAll(memberADiaryRequests);

        Member anotherMember = createAnotherMember();
        List<Schedule> memberBScheduleRequests = List.of(
                Schedule.builder()
                        .member(anotherMember)
                        .title("일정 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(anotherMember)
                        .title("일정 7")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 1))
                        .build()
        );
        scheduleRepository.saveAll(memberBScheduleRequests);

        List<Diary> memberBDiaryRequests = List.of(
                Diary.builder()
                        .member(anotherMember)
                        .schedule(memberBScheduleRequests.get(0))
                        .content("")
                        .build()
        );
        diaryRepository.saveAll(memberBDiaryRequests);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/")
                        .param("date", "2023-06-26")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedScheduleTitles = List.of("일정 3", "일정 4", "일정 2", "일정 5");

        ScheduleListResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleListResponseDto[].class);

        assertThat(responseDtos.length).isEqualTo(expectedScheduleTitles.size());
        assertThat(responseDtos)
                .extracting(ScheduleListResponseDto::getTitle)
                .isEqualTo(expectedScheduleTitles);
        assertThat(responseDtos)
                .extracting(ScheduleListResponseDto::getDate)
                .containsOnly(LocalDate.of(2023, 6, 26));
    }

    @PlangoMockUser
    @Test
    void 월별로_일정이_존재하는_날짜의_목록_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        List<Schedule> requests = List.of(
                Schedule.builder()
                        .member(member)
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 5, 31))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .done(true)
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 15))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 17))
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 30))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .done(true)
                        .build(),
                Schedule.builder()
                        .member(member)
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 7, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build()
        );
        scheduleRepository.saveAll(requests);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/")
                        .param("yearMonth", "2023-06")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<ScheduleCountResponseDto> expected = List.of(
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 1), 1, 2),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 15), 0, 1),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 17), 0, 1),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 30), 1, 1)
        );

        List<ScheduleCountResponseDto> responseDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleCountResponseDto[].class));
        assertThat(responseDtos)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    private Member createAnotherMember() {
        Member member = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .build();
        return memberRepository.save(member);
    }

    private Long parseScheduleIdFrom(String locationHeader) {
        String[] splitedLocation = locationHeader.split("/");
        return Long.parseLong(splitedLocation[splitedLocation.length - 1]);
    }
}
