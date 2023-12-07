package dev.gmelon.plango.web.schedule;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.member.MemberType;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.place.SchedulePlace;
import dev.gmelon.plango.domain.schedule.place.SchedulePlaceRepository;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import dev.gmelon.plango.service.schedule.ScheduleMemberService;
import dev.gmelon.plango.service.schedule.dto.ScheduleCountResponseDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleCreateRequestDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleEditDoneRequestDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleEditRequestDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleDateListResponseDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleResponseDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleResponseDto.SchedulePlaceResponseDto;
import dev.gmelon.plango.service.schedule.dto.ScheduleSearchResponseDto;
import dev.gmelon.plango.service.schedule.place.dto.SchedulePlaceCreateRequestDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
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
    @Autowired
    private ScheduleMemberRepository scheduleMemberRepository;
    @Autowired
    private ScheduleMemberService scheduleMemberService;
    @Autowired
    private SchedulePlaceRepository schedulePlaceRepository;

    @PlangoMockUser
    @Test
    void 참가자와_장소가_1개인_일정_생성() throws Exception {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .participantIds(List.of())
                .schedulePlaces(List.of(
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3674097)
                                .longitude(127.3454477)
                                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                                .placeName("충남대학교 공과대학 5호관")
                                .memo("장소 메모")
                                .category("수업")
                                .build()
                ))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Long createdScheduleId = parseScheduleIdFrom(response.getHeader(HttpHeaders.LOCATION));

        Schedule createdSchedule = assertDoesNotThrow(() -> scheduleRepository.findByIdWithMembers(createdScheduleId).get());

        assertThat(createdSchedule.getScheduleMembers()).hasSize(1);
        assertThat(createdSchedule.getScheduleMembers()).extracting(ScheduleMember::memberId)
                .containsExactly(memberRepository.findAll().get(0).getId());

        assertThat(createdSchedule.getTitle()).isEqualTo(request.getTitle());
        assertThat(createdSchedule.getContent()).isEqualTo(request.getContent());
        assertThat(createdSchedule.getDate()).isEqualTo(request.getDate());
        assertThat(createdSchedule.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(createdSchedule.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(createdSchedule.isDone()).isFalse();

        List<SchedulePlace> schedulePlaces = schedulePlaceRepository.findAllByScheduleId(createdScheduleId);
        assertThat(schedulePlaces).hasSize(1);

        SchedulePlace createdSchedulePlace = schedulePlaces.get(0);
        SchedulePlaceCreateRequestDto requestedSchedulePlace = request.getSchedulePlaces().get(0);
        assertThat(createdSchedulePlace.getLatitude()).isEqualTo(requestedSchedulePlace.getLatitude());
        assertThat(createdSchedulePlace.getLongitude()).isEqualTo(requestedSchedulePlace.getLongitude());
        assertThat(createdSchedulePlace.getPlaceName()).isEqualTo(requestedSchedulePlace.getPlaceName());
        assertThat(createdSchedulePlace.getRoadAddress()).isEqualTo(requestedSchedulePlace.getRoadAddress());
        assertThat(createdSchedulePlace.getMemo()).isEqualTo(requestedSchedulePlace.getMemo());
        assertThat(createdSchedulePlace.getCategory()).isEqualTo(requestedSchedulePlace.getCategory());
        assertThat(createdSchedulePlace.isConfirmed()).isFalse();
    }

    @PlangoMockUser
    @Test
    void 참가자가_여러명인_일정_생성() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .schedulePlaces(List.of())
                .participantIds(List.of(anotherMember.getId()))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Long createdScheduleId = parseScheduleIdFrom(response.getHeader(HttpHeaders.LOCATION));

        Schedule createdSchedule = assertDoesNotThrow(() -> scheduleRepository.findByIdWithMembers(createdScheduleId).get());
        Member member = memberRepository.findAll().get(0);

        assertThat(createdSchedule.getScheduleMembers()).hasSize(2);
        assertThat(createdSchedule.getScheduleMembers()).extracting(ScheduleMember::memberId)
                .containsExactlyInAnyOrder(member.getId(), anotherMember.getId());
    }

    @PlangoMockUser
    @Test
    void 장소가_없는_일정_생성() throws Exception {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .schedulePlaces(List.of())
                .participantIds(List.of())
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Long createdScheduleId = parseScheduleIdFrom(response.getHeader(HttpHeaders.LOCATION));

        Schedule createdSchedule = assertDoesNotThrow(() -> scheduleRepository.findByIdWithMembers(createdScheduleId).get());

        assertThat(createdSchedule.getScheduleMembers()).hasSize(1);
        assertThat(createdSchedule.getScheduleMembers()).extracting(ScheduleMember::memberId)
                .containsExactly(memberRepository.findAll().get(0).getId());

        assertThat(createdSchedule.getTitle()).isEqualTo(request.getTitle());
        assertThat(createdSchedule.getContent()).isEqualTo(request.getContent());
        assertThat(createdSchedule.getDate()).isEqualTo(request.getDate());
        assertThat(createdSchedule.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(createdSchedule.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(createdSchedule.isDone()).isFalse();

        assertThat(schedulePlaceRepository.findAllByScheduleId(createdScheduleId)).hasSize(0);
    }

    @PlangoMockUser
    @Test
    void 장소가_여러개인_일정_생성() throws Exception {
        // given
        ScheduleCreateRequestDto request = ScheduleCreateRequestDto.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .participantIds(List.of())
                .schedulePlaces(List.of(
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3674097)
                                .longitude(127.3454477)
                                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                                .placeName("충남대학교 공과대학 5호관")
                                .memo("장소 메모")
                                .category("수업")
                                .build(),
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3645845)
                                .longitude(127.3412946)
                                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                                .placeName("카페 인터뷰")
                                .memo("장소 메모")
                                .category("카페")
                                .build()
                ))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Long createdScheduleId = parseScheduleIdFrom(response.getHeader(HttpHeaders.LOCATION));

        Schedule createdSchedule = assertDoesNotThrow(() -> scheduleRepository.findByIdWithMembers(createdScheduleId).get());

        assertThat(createdSchedule.getScheduleMembers()).hasSize(1);
        assertThat(createdSchedule.getScheduleMembers()).extracting(ScheduleMember::memberId)
                .containsExactly(memberRepository.findAll().get(0).getId());

        assertThat(createdSchedule.getTitle()).isEqualTo(request.getTitle());
        assertThat(createdSchedule.getContent()).isEqualTo(request.getContent());
        assertThat(createdSchedule.getDate()).isEqualTo(request.getDate());
        assertThat(createdSchedule.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(createdSchedule.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(createdSchedule.isDone()).isFalse();

        List<SchedulePlace> createdSchedulePlaces = schedulePlaceRepository.findAllByScheduleId(createdScheduleId);
        assertThat(createdSchedulePlaces).hasSize(2);
        List<SchedulePlaceCreateRequestDto> requestedSchedulePlaces = request.getSchedulePlaces();
        for (int i = 0, schedulePlacesSize = createdSchedulePlaces.size(); i < schedulePlacesSize; i++) {
            SchedulePlace createdSchedulePlace = createdSchedulePlaces.get(i);
            SchedulePlaceCreateRequestDto requestedSchedulePlace = requestedSchedulePlaces.get(i);

            assertThat(createdSchedulePlace.getLatitude()).isEqualTo(requestedSchedulePlace.getLatitude());
            assertThat(createdSchedulePlace.getLongitude()).isEqualTo(requestedSchedulePlace.getLongitude());
            assertThat(createdSchedulePlace.getPlaceName()).isEqualTo(requestedSchedulePlace.getPlaceName());
            assertThat(createdSchedulePlace.getRoadAddress()).isEqualTo(requestedSchedulePlace.getRoadAddress());
            assertThat(createdSchedulePlace.getMemo()).isEqualTo(requestedSchedulePlace.getMemo());
            assertThat(createdSchedulePlace.getCategory()).isEqualTo(requestedSchedulePlace.getCategory());
            assertThat(createdSchedulePlace.isConfirmed()).isFalse();
        }
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
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        // TODO addSchedulePlaces() 로 변경
        givenSchedule.setSchedulePlaces(List.of(
                SchedulePlace.builder()
                        .latitude(36.3674097)
                        .longitude(127.3454477)
                        .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                        .placeName("충남대학교 공과대학 5호관")
                        .memo("장소 메모")
                        .category("수업")
                        .schedule(givenSchedule)
                        .build()
        ));
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/" + savedSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        ScheduleResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleResponseDto.class);

        assertThat(responseDto.getScheduleId()).isEqualTo(savedSchedule.getId());
        assertThat(responseDto.getTitle()).isEqualTo(givenSchedule.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(givenSchedule.getContent());
        assertThat(responseDto.getDate()).isEqualTo(givenSchedule.getDate());
        assertThat(responseDto.getStartTime()).isEqualTo(givenSchedule.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(givenSchedule.getEndTime());
        assertThat(responseDto.getSchedulePlaces()).hasSize(1);
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isFalse();

        SchedulePlaceResponseDto responseSchedulePlace = responseDto.getSchedulePlaces().get(0);
        SchedulePlace givenSchedulePlace = givenSchedule.getSchedulePlaces().get(0);
        assertThat(responseSchedulePlace.getLatitude()).isEqualTo(givenSchedulePlace.getLatitude());
        assertThat(responseSchedulePlace.getLongitude()).isEqualTo(givenSchedulePlace.getLongitude());
        assertThat(responseSchedulePlace.getPlaceName()).isEqualTo(givenSchedulePlace.getPlaceName());
        assertThat(responseSchedulePlace.getRoadAddress()).isEqualTo(givenSchedulePlace.getRoadAddress());
        assertThat(responseSchedulePlace.getMemo()).isEqualTo(givenSchedulePlace.getMemo());
        assertThat(responseSchedulePlace.getCategory()).isEqualTo(givenSchedulePlace.getCategory());
        assertThat(responseSchedulePlace.getIsConfirmed()).isFalse();
    }

    @PlangoMockUser
    @Test
    void 기록이_있는_일정_단건_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        givenSchedule.setSchedulePlaces(List.of(
                SchedulePlace.builder()
                        .latitude(36.3674097)
                        .longitude(127.3454477)
                        .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                        .placeName("충남대학교 공과대학 5호관")
                        .memo("장소 메모")
                        .category("수업")
                        .schedule(givenSchedule)
                        .build()
        ));
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

        assertThat(responseDto.getScheduleId()).isEqualTo(savedSchedule.getId());
        assertThat(responseDto.getTitle()).isEqualTo(savedSchedule.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(savedSchedule.getContent());
        assertThat(responseDto.getDate()).isEqualTo(savedSchedule.getDate());
        assertThat(responseDto.getStartTime()).isEqualTo(savedSchedule.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(savedSchedule.getEndTime());
        assertThat(responseDto.getSchedulePlaces()).hasSize(1);
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isTrue();

        SchedulePlaceResponseDto responseSchedulePlace = responseDto.getSchedulePlaces().get(0);
        SchedulePlace givenSchedulePlace = givenSchedule.getSchedulePlaces().get(0);
        assertThat(responseSchedulePlace.getLatitude()).isEqualTo(givenSchedulePlace.getLatitude());
        assertThat(responseSchedulePlace.getLongitude()).isEqualTo(givenSchedulePlace.getLongitude());
        assertThat(responseSchedulePlace.getPlaceName()).isEqualTo(givenSchedulePlace.getPlaceName());
        assertThat(responseSchedulePlace.getRoadAddress()).isEqualTo(givenSchedulePlace.getRoadAddress());
        assertThat(responseSchedulePlace.getMemo()).isEqualTo(givenSchedulePlace.getMemo());
        assertThat(responseSchedulePlace.getCategory()).isEqualTo(givenSchedulePlace.getCategory());
        assertThat(responseSchedulePlace.getIsConfirmed()).isFalse();
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
        // TODO 실패 응답 통일하기
        assertThat(responseDto.getMessage()).startsWith("존재하지 않는");
    }

    @PlangoMockUser
    @Test
    void 타인의_일정_단건_조회() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
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
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
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
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(savedSchedule.getId()).get());
        assertThat(foundSchedule.getTitle()).isEqualTo(request.getTitle());
        assertThat(foundSchedule.getContent()).isEqualTo(request.getContent());
        assertThat(foundSchedule.getDate()).isEqualTo(request.getDate());
        assertThat(foundSchedule.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(foundSchedule.getEndTime()).isEqualTo(request.getEndTime());
    }

    @PlangoMockUser
    @Test
    void 참가하는_타인의_일정_수정_수락전() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(anotherMember, givenSchedule),
                ScheduleMember.createParticipant(member, givenSchedule)
        ));
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
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 참가하는_타인의_일정_수정_수락후() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(anotherMember, givenSchedule),
                ScheduleMember.createParticipant(member, givenSchedule)
        ));
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        scheduleMemberService.acceptInvitation(member.getId(), givenSchedule.getId());

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
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @PlangoMockUser
    @Test
    void 참가하지않는_타인의_일정_수정() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
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
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
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
                .title("일정 제목")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .done(false)
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
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
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
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
    void 참가하지않는_타인의_일정_삭제() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
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
    void 참가하는_타인의_일정_삭제() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(anotherMember, givenSchedule),
                ScheduleMember.createParticipant(member, givenSchedule)
        ));
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        scheduleMemberService.acceptInvitation(member.getId(), givenSchedule.getId());

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + savedSchedule.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(scheduleRepository.findById(savedSchedule.getId())).isPresent();
    }

    @PlangoMockUser
    @Test
    void 날짜별_기록이_없는_일정_목록_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        List<Schedule> memberScheduleRequests = List.of(
                Schedule.builder()
                        .title("일정 1")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 1))
                        .build(),
                Schedule.builder()
                        .title("일정 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 5")
                        .date(LocalDate.of(2023, 6, 26))
                        .build()
        );
        scheduleRepository.saveAll(memberScheduleRequests);
        for (Schedule memberScheduleRequest : memberScheduleRequests) {
            ScheduleMember scheduleMember = ScheduleMember.builder()
                    .schedule(memberScheduleRequest)
                    .member(member)
                    .owner(true)
                    .accepted(true)
                    .build();
            scheduleMemberRepository.save(scheduleMember);
        }

        List<Diary> memberDiaryRequests = List.of(
                Diary.builder()
                        .member(member)
                        .schedule(memberScheduleRequests.get(2))
                        .content("")
                        .build(),
                Diary.builder()
                        .member(member)
                        .schedule(memberScheduleRequests.get(3))
                        .content("")
                        .build()
        );
        diaryRepository.saveAll(memberDiaryRequests);

        Member anotherMember = createAnotherMember();
        List<Schedule> anotherMemberScheduleRequests = List.of(
                Schedule.builder()
                        .title("일정 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 7")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 1))
                        .build()
        );
        scheduleRepository.saveAll(anotherMemberScheduleRequests);
        for (Schedule anotherMemberScheduleRequest : anotherMemberScheduleRequests) {
            ScheduleMember scheduleMember = ScheduleMember.builder()
                    .schedule(anotherMemberScheduleRequest)
                    .member(anotherMember)
                    .owner(true)
                    .accepted(true)
                    .build();
            scheduleMemberRepository.save(scheduleMember);
        }

        List<Diary> anotherMemberDiaryRequests = List.of(
                Diary.builder()
                        .member(anotherMember)
                        .schedule(anotherMemberScheduleRequests.get(0))
                        .content("")
                        .build()
        );
        diaryRepository.saveAll(anotherMemberDiaryRequests);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/")
                        .param("date", "2023-06-26")
                        .param("noDiaryOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedScheduleTitles = List.of("일정 5", "일정 2");

        ScheduleDateListResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleDateListResponseDto[].class);

        assertThat(responseDtos.length).isEqualTo(expectedScheduleTitles.size());
        assertThat(responseDtos)
                .extracting(ScheduleDateListResponseDto::getTitle)
                .isEqualTo(expectedScheduleTitles);
        assertThat(responseDtos)
                .extracting(ScheduleDateListResponseDto::getDate)
                .containsOnly(LocalDate.of(2023, 6, 26));
    }

    @PlangoMockUser
    @Test
    void 날짜별_전체_일정_목록_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        List<Schedule> memberScheduleRequests = List.of(
                Schedule.builder()
                        .title("일정 1")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .build(),
                Schedule.builder()
                        .title("일정 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .build(),
                Schedule.builder()
                        .title("일정 5")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build()
        );
        for (Schedule memberScheduleRequest : memberScheduleRequests) {
            memberScheduleRequest.setSingleOwnerScheduleMember(member);
        }
        scheduleRepository.saveAll(memberScheduleRequests);

        List<Diary> memberDiaryRequests = List.of(
                Diary.builder()
                        .member(member)
                        .schedule(memberScheduleRequests.get(0))
                        .content("")
                        .build(),
                Diary.builder()
                        .member(member)
                        .schedule(memberScheduleRequests.get(2))
                        .content("")
                        .build()
        );
        diaryRepository.saveAll(memberDiaryRequests);

        Member anotherMember = createAnotherMember();
        List<Schedule> anotherMemberScheduleRequests = List.of(
                Schedule.builder()
                        .title("일정 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 7")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 1))
                        .build()
        );
        for (Schedule anotherMemberScheduleRequest : anotherMemberScheduleRequests) {
            anotherMemberScheduleRequest.setSingleOwnerScheduleMember(anotherMember);
        }
        scheduleRepository.saveAll(anotherMemberScheduleRequests);

        List<Diary> anotherMemberDiaryRequests = List.of(
                Diary.builder()
                        .member(anotherMember)
                        .schedule(anotherMemberScheduleRequests.get(0))
                        .content("")
                        .build()
        );
        diaryRepository.saveAll(anotherMemberDiaryRequests);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/")
                        .param("date", "2023-06-26")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedScheduleTitles = List.of("일정 3", "일정 4", "일정 2", "일정 5");

        ScheduleDateListResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleDateListResponseDto[].class);

        assertThat(responseDtos.length).isEqualTo(expectedScheduleTitles.size());
        assertThat(responseDtos)
                .extracting(ScheduleDateListResponseDto::getTitle)
                .isEqualTo(expectedScheduleTitles);
        assertThat(responseDtos)
                .extracting(ScheduleDateListResponseDto::getDate)
                .containsOnly(LocalDate.of(2023, 6, 26));
    }

    @PlangoMockUser
    @Test
    void 월별로_일정이_존재하는_날짜의_목록_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        List<Schedule> requests = List.of(
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 5, 31))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .done(true)
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 15))
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 17))
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 6, 30))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .done(true)
                        .build(),
                Schedule.builder()
                        .title("일정 제목")
                        .date(LocalDate.of(2023, 7, 1))
                        .startTime(LocalTime.of(11, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build()
        );
        for (Schedule request : requests) {
            request.setSingleOwnerScheduleMember(member);
        }
        scheduleRepository.saveAll(requests);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/count")
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

    @PlangoMockUser
    @Test
    void 일정_록록_조회시_confirm된_장소_목록이_조회된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .date(LocalDate.of(2023, 10, 1))
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        List<SchedulePlace> givenSchedulePlaces = List.of(
                SchedulePlace.builder()
                        .placeName("확정된 장소 1")
                        .schedule(givenSchedule)
                        .confirmed(true)
                        .build(),
                SchedulePlace.builder()
                        .placeName("확정된 장소 2")
                        .schedule(givenSchedule)
                        .confirmed(true)
                        .build(),
                SchedulePlace.builder()
                        .placeName("확정되지 않은 장소")
                        .schedule(givenSchedule)
                        .confirmed(false)
                        .build()
        );
        schedulePlaceRepository.saveAll(givenSchedulePlaces);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules")
                        .queryParam("date", "2023-10-01"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ScheduleDateListResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleDateListResponseDto[].class)[0];
        assertThat(responseDto.getConfirmedPlaceNames()).isEqualTo("확정된 장소 1, 확정된 장소 2");
    }

    @PlangoMockUser
    @Test
    void 키워드_검색시_공백을_제거하고_일정_제목_또는_본문에서_검색된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        List<Schedule> givenMemberSchedules = List.of(
                Schedule.builder()
                        .title("현재 회원 - 학교 수업")
                        .content("")
                        .build(),
                Schedule.builder()
                        .title("일정 B")
                        .content("학교수업")
                        .build(),
                Schedule.builder()
                        .title("일정 C")
                        .content("일정 C 메모")
                        .build()
        );
        for (Schedule givenSchedule : givenMemberSchedules) {
            givenSchedule.setSingleOwnerScheduleMember(member);
        }
        scheduleRepository.saveAll(givenMemberSchedules);

        Schedule givenAnotherMemberSchedule = Schedule.builder()
                .title("타 회원 - 학교 수업")
                .content("")
                .build();
        givenAnotherMemberSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenAnotherMemberSchedule);

        String query = "학 교 수 업";

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules")
                .queryParam("query", query)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ScheduleSearchResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleSearchResponseDto[].class);
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(ScheduleSearchResponseDto::getTitle)
                .containsExactlyInAnyOrder("현재 회원 - 학교 수업", "일정 B");
    }

    private Member createAnotherMember() {
        Member member = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .build();
        return memberRepository.save(member);
    }

    private Long parseScheduleIdFrom(String locationHeader) {
        String[] splitedLocation = locationHeader.split("/");
        return Long.parseLong(splitedLocation[splitedLocation.length - 1]);
    }
}
