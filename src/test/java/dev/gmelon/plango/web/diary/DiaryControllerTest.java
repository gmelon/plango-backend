package dev.gmelon.plango.web.diary;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.diary.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ScheduleMemberRepository scheduleMemberRepository;

    private Member memberA;
    private Schedule scheduleOfMemberA;

    @BeforeEach
    void setUp() {
        memberA = memberRepository.findAll().get(0);

        scheduleOfMemberA = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 25))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .build();
        scheduleOfMemberA.setSingleOwnerScheduleMember(memberA);
        scheduleRepository.save(scheduleOfMemberA);
    }

    @PlangoMockUser
    @Test
    void 기록_생성_시_내용_또는_사진은_필수_값() throws Exception {
        // given
        DiaryCreateRequestDto request = new DiaryCreateRequestDto();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        System.out.println(response.getContentAsString(UTF_8));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 기록_수정_시_내용_또는_사진은_필수_값() throws Exception {
        // given
        DiaryEditRequestDto request = new DiaryEditRequestDto();

        // when
        MockHttpServletResponse response = mockMvc.perform(put("/api/diaries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 자신의_일정에_기록_생성() throws Exception {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .content("기록 본문")
                .imageUrls(List.of("https://plango-backend/imageA.jpg", "https://plango-backend/imageB.jpg"))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Long createdDiaryId = parseIdFrom(response.getHeader(HttpHeaders.LOCATION));
        Diary createdDiary = assertDoesNotThrow(() -> diaryRepository.findById(createdDiaryId).get());
        assertThat(createdDiary.getContent()).isEqualTo(request.getContent());
        assertThat(createdDiary.getDiaryImageUrls()).isEqualTo(request.getImageUrls());
    }

    @PlangoMockUser
    @Test
    void 이미_기록이_존재하는_일정에_기록_생성() throws Exception {
        // given
        Diary diary = Diary.builder()
                .member(memberA)
                .schedule(scheduleOfMemberA)
                .content("기존 기록")
                .build();
        diaryRepository.save(diary);

        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .content("새로운 기록")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/" + scheduleOfMemberA.getId() + "/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 수락하지_않은_일정에_기록_생성() throws Exception {
        // given
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
                ScheduleMember.createParticipant(memberA, givenSchedule)
        ));
        Schedule savedSchedule = scheduleRepository.save(givenSchedule);

        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .content("새로운 기록")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/" + savedSchedule.getId() + "/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 타인의_일정에_기록_생성() throws Exception {
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

        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .content("기록 본문")
                .imageUrls(List.of("https://plango-backend/imageA.jpg"))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/" + savedSchedule.getId() + "/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 기록_단건_조회() throws Exception {
        // given
        Diary givenDiary = Diary.builder()
                .member(memberA)
                .schedule(scheduleOfMemberA)
                .content("기록 본문")
                .imageUrls(List.of("https://plango-backend/imageA.jpg", "https://plango-backend/imageB.jpg"))
                .build();
        diaryRepository.save(givenDiary);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/diaries/" + givenDiary.getId()))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        DiaryResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), DiaryResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(givenDiary.getId());
        assertThat(responseDto.getContent()).isEqualTo(givenDiary.getContent());
        assertThat(responseDto.getImageUrls()).isEqualTo(givenDiary.getDiaryImageUrls());

        assertThat(responseDto.getSchedule().getId()).isEqualTo(scheduleOfMemberA.getId());
        assertThat(responseDto.getSchedule().getTitle()).isEqualTo(scheduleOfMemberA.getTitle());
        assertThat(responseDto.getSchedule().getDate()).isEqualTo(scheduleOfMemberA.getDate());
        assertThat(responseDto.getSchedule().getStartTime()).isEqualTo(scheduleOfMemberA.getStartTime());
        assertThat(responseDto.getSchedule().getEndTime()).isEqualTo(scheduleOfMemberA.getEndTime());
    }

    @PlangoMockUser
    @Test
    void 타인의_기록_단건_조회() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        Diary givenDiary = Diary.builder()
                .member(anotherMember)
                .schedule(givenSchedule)
                .content("기록 본문")
                .build();
        diaryRepository.save(givenDiary);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/diaries/" + givenDiary.getId()))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 존재하지_않는_기록_단건_조회() throws Exception {
        // given
        String requestUrl = "/api/diaries/1";

        // when
        MockHttpServletResponse response = mockMvc.perform(get(requestUrl))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 일정_id로_기록_단건_조회() throws Exception {
        // given
        Diary givenDiary = Diary.builder()
                .member(memberA)
                .schedule(scheduleOfMemberA)
                .content("기록 본문")
                .imageUrls(List.of("https://plango-backend/imageA.jpg", "https://plango-backend/imageB.jpg"))
                .build();
        diaryRepository.save(givenDiary);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/" + scheduleOfMemberA.getId() + "/diary"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        DiaryResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), DiaryResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(givenDiary.getId());
        assertThat(responseDto.getContent()).isEqualTo(givenDiary.getContent());
        assertThat(responseDto.getImageUrls()).isEqualTo(givenDiary.getDiaryImageUrls());

        assertThat(responseDto.getSchedule().getId()).isEqualTo(scheduleOfMemberA.getId());
        assertThat(responseDto.getSchedule().getTitle()).isEqualTo(scheduleOfMemberA.getTitle());
        assertThat(responseDto.getSchedule().getDate()).isEqualTo(scheduleOfMemberA.getDate());
        assertThat(responseDto.getSchedule().getStartTime()).isEqualTo(scheduleOfMemberA.getStartTime());
        assertThat(responseDto.getSchedule().getEndTime()).isEqualTo(scheduleOfMemberA.getEndTime());
    }

    @PlangoMockUser
    @Test
    void 타인의_일정_id로_기록_단건_조회() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        Diary givenDiary = Diary.builder()
                .member(anotherMember)
                .schedule(givenSchedule)
                .content("기록 본문")
                .build();
        diaryRepository.save(givenDiary);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/" + givenSchedule.getId() + "/diary"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 존재하지_않는_일정_id로_기록_단건_조회() throws Exception {
        // given
        String requestUrl = "/api/schedules/" + (scheduleOfMemberA.getId() + 1) + "/diary";

        // when
        MockHttpServletResponse response = mockMvc.perform(get(requestUrl))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 기록_수정() throws Exception {
        // given
        Diary givenDiary = Diary.builder()
                .member(memberA)
                .schedule(scheduleOfMemberA)
                .content("기록 본문")
                .imageUrls(List.of("https://plango-backend/imageA.jpg", "https://plango-backend/imageB.jpg"))
                .build();
        diaryRepository.save(givenDiary);

        DiaryEditRequestDto request = DiaryEditRequestDto.builder()
                .content("기록 본문 2")
                .imageUrls(List.of("https://plango-backend/imageA.jpg", "https://plango-backend/imageC.jpg"))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(put("/api/diaries/" + givenDiary.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        Diary createdDiary = assertDoesNotThrow(() -> diaryRepository.findById(givenDiary.getId()).get());
        assertThat(createdDiary.getContent()).isEqualTo(request.getContent());
        assertThat(createdDiary.getDiaryImageUrls()).isEqualTo(request.getImageUrls());
    }

    @PlangoMockUser
    @Test
    void 타인의_기록_수정() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        Diary givenDiary = Diary.builder()
                .member(anotherMember)
                .schedule(givenSchedule)
                .content("기록 본문")
                .build();
        diaryRepository.save(givenDiary);

        DiaryEditRequestDto request = DiaryEditRequestDto.builder()
                .content("기록 본문 2")
                .imageUrls(List.of("https://plango-backend/imageB.jpg"))
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(put("/api/diaries/" + givenDiary.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 기록_삭제() throws Exception {
        // given
        Diary givenDiary = Diary.builder()
                .member(memberA)
                .schedule(scheduleOfMemberA)
                .content("기록 본문")
                .imageUrls(List.of("https://plango-backend/imageA.jpg", "https://plango-backend/imageB.jpg"))
                .build();
        diaryRepository.save(givenDiary);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/diaries/" + givenDiary.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(diaryRepository.findById(givenDiary.getId())).isEmpty();
    }

    @PlangoMockUser
    @Test
    void 타인의_기록_삭제() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        Diary givenDiary = Diary.builder()
                .member(anotherMember)
                .schedule(givenSchedule)
                .content("기록 본문")
                .build();
        diaryRepository.save(givenDiary);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/diaries/" + givenDiary.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(diaryRepository.findById(givenDiary.getId())).isPresent();
    }

    @PlangoMockUser
    @Test
    void 날짜별_기록_목록_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        List<Schedule> memberSchedules = List.of(
                Schedule.builder()
                        .title("일정 0")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 1")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 1))
                        .build(),
                Schedule.builder()
                        .title("일정 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .build()
        );
        for (Schedule memberSchedule : memberSchedules) {
            memberSchedule.setSingleOwnerScheduleMember(member);
        }
        scheduleRepository.saveAll(memberSchedules);

        List<Diary> memberADiaries = memberSchedules.stream()
                .map(schedule -> Diary.builder()
                        .schedule(schedule)
                        .member(member)
                        .content(schedule.getTitle() + " 기록")
                        .build())
                .collect(toList());
        diaryRepository.saveAll(memberADiaries);



        List<Schedule> anotherMemberSchedules = List.of(
                Schedule.builder()
                        .title("일정 5")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .build(),
                Schedule.builder()
                        .title("일정 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 0))
                        .build()
        );
        for (Schedule anotherMemberSchedule : anotherMemberSchedules) {
            anotherMemberSchedule.setSingleOwnerScheduleMember(anotherMember);
        }
        scheduleRepository.saveAll(anotherMemberSchedules);

        List<Diary> anotherMemberDiaries = anotherMemberSchedules.stream()
                .map(schedule -> Diary.builder()
                        .schedule(schedule)
                        .member(anotherMember)
                        .content(schedule.getTitle() + " 기록")
                        .build())
                .collect(toList());
        diaryRepository.saveAll(anotherMemberDiaries);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("date", "2023-06-26"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<String> expectedContents = List.of("일정 1 기록", "일정 2 기록", "일정 3 기록", "일정 4 기록");

        DiaryListResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), DiaryListResponseDto[].class);

        assertThat(responseDtos.length).isEqualTo(expectedContents.size());
        assertThat(responseDtos)
                .extracting(DiaryListResponseDto::getContent)
                .isEqualTo(expectedContents);
    }

    @PlangoMockUser
    @Test
    void 키워드_검색시_공백을_제거하고_기록_본문에서_검색된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 메모")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        List<Diary> givenDiaries = List.of(
                Diary.builder()
                        .content("기록 A")
                        .schedule(givenSchedule)
                        .build(),
                Diary.builder()
                        .content("기록 B")
                        .schedule(givenSchedule)
                        .build()
        );
        diaryRepository.saveAll(givenDiaries);

        String query = "기 록";

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/diaries")
                .queryParam("query", query)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        DiarySearchResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), DiarySearchResponseDto[].class);
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(DiarySearchResponseDto::getContent)
                .containsExactlyInAnyOrder("기록 A", "기록 B");
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

    private Long parseIdFrom(String locationHeader) {
        String[] splitedLocation = locationHeader.split("/");
        return Long.parseLong(splitedLocation[splitedLocation.length - 1]);
    }
}
