package dev.gmelon.plango.service.diary;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.diary.NoSuchDiaryException;
import dev.gmelon.plango.exception.schedule.NoSuchScheduleException;
import dev.gmelon.plango.exception.schedule.ScheduleAccessDeniedException;
import dev.gmelon.plango.service.diary.dto.DiaryCreateRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryEditRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryListResponseDto;
import dev.gmelon.plango.service.diary.dto.DiaryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class DiaryServiceTest {

    private Member memberA;
    private Member memberB;

    private Schedule scheduleOfMemberA;

    @Autowired
    private DiaryService diaryService;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberA = Member.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberA);

        memberB = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberB);

        scheduleOfMemberA = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 25))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .placeName("일정 장소")
                .member(memberA)
                .build();
        scheduleRepository.save(scheduleOfMemberA);
    }

    @Test
    void 자신의_일정에_기록_생성() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();

        // when
        Long createdDiaryId = diaryService.create(memberA.getId(), scheduleOfMemberA.getId(), request);

        // then
        assertThat(createdDiaryId).isNotNull();

        Diary createdDiary = assertDoesNotThrow(() -> diaryRepository.findById(createdDiaryId).get());
        assertThat(createdDiary.getContent()).isEqualTo(request.getContent());
        assertThat(createdDiary.getImageUrl()).isEqualTo(request.getImageUrl());
    }

    @Test
    void 타인의_일정에_기록_생성() {
        // given
        DiaryCreateRequestDto request = DiaryCreateRequestDto.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();

        // when
        assertThatThrownBy(() -> diaryService.create(memberB.getId(), scheduleOfMemberA.getId(), request))
                .isInstanceOf(ScheduleAccessDeniedException.class);
    }

    @Test
    void 기록_단건_조회() {
        // given
        Diary diary = Diary.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when
        DiaryResponseDto responseDto = diaryService.findById(memberA.getId(), diary.getId());

        // then
        assertThat(responseDto.getId()).isEqualTo(diary.getId());
        assertThat(responseDto.getContent()).isEqualTo(diary.getContent());
        assertThat(responseDto.getImageUrl()).isEqualTo(diary.getImageUrl());

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
        Diary diary = Diary.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when, then
        assertThatThrownBy(() -> diaryService.findById(memberB.getId(), diary.getId()))
                .isInstanceOf(ScheduleAccessDeniedException.class);
    }

    @Test
    void 존재하지_않는_기록_단건_조회() {
        // when, then
        assertThatThrownBy(() -> diaryService.findById(memberB.getId(), 1L))
                .isInstanceOf(NoSuchDiaryException.class);
    }

    @Test
    void 일정_id로_기록_단건_조회() {
        // given
        Diary diary = Diary.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when
        DiaryResponseDto responseDto = diaryService.findByScheduleId(memberA.getId(), schedule.getId());

        // then
        assertThat(responseDto.getId()).isEqualTo(diary.getId());
        assertThat(responseDto.getContent()).isEqualTo(diary.getContent());
        assertThat(responseDto.getImageUrl()).isEqualTo(diary.getImageUrl());

        assertThat(responseDto.getSchedule().getId()).isEqualTo(scheduleOfMemberA.getId());
        assertThat(responseDto.getSchedule().getTitle()).isEqualTo(scheduleOfMemberA.getTitle());
        assertThat(responseDto.getSchedule().getDate()).isEqualTo(scheduleOfMemberA.getDate());
        assertThat(responseDto.getSchedule().getStartTime()).isEqualTo(scheduleOfMemberA.getStartTime());
        assertThat(responseDto.getSchedule().getEndTime()).isEqualTo(scheduleOfMemberA.getEndTime());
    }

    @Test
    void 타인의_일정_id로_기록_단건_조회() {
        // given
        Diary diary = Diary.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when, then
        assertThatThrownBy(() -> diaryService.findByScheduleId(memberB.getId(), schedule.getId()))
                .isInstanceOf(ScheduleAccessDeniedException.class);
    }

    @Test
    void 존재하지_않는_일정_id로_기록_단건_조회() {
        // when, then
        assertThatThrownBy(() -> diaryService.findByScheduleId(memberB.getId(), scheduleOfMemberA.getId() + 1))
                .isInstanceOf(NoSuchScheduleException.class);
    }

    @Test
    void 기록_수정() {
        // given
        Diary diary = Diary.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        DiaryEditRequestDto requestDto = DiaryEditRequestDto.builder()
                .content("기록 본문 B")
                .imageUrl("https://plango-backend/imageB.jpg")
                .build();

        // when
        diaryService.edit(memberA.getId(), diary.getId(), requestDto);

        // then
        Diary foundDiary = assertDoesNotThrow(() -> diaryRepository.findById(diary.getId()).get());

        assertThat(foundDiary.getContent()).isEqualTo(requestDto.getContent());
        assertThat(foundDiary.getImageUrl()).isEqualTo(requestDto.getImageUrl());
    }

    @Test
    void 타인의_기록_수정() {
        // given
        Diary diary = Diary.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        DiaryEditRequestDto requestDto = DiaryEditRequestDto.builder()
                .content("기록 본문 B")
                .imageUrl("https://plango-backend/imageB.jpg")
                .build();

        // when, then
        assertThatThrownBy(() -> diaryService.edit(memberB.getId(), diary.getId(), requestDto))
                .isInstanceOf(ScheduleAccessDeniedException.class);

        Diary foundDiary = assertDoesNotThrow(() -> diaryRepository.findById(diary.getId()).get());
        assertThat(foundDiary)
                .usingRecursiveComparison()
                .isEqualTo(diary);
    }

    @Test
    void 기록_삭제() {
        // TODO s3 삭제 여부 어떻게 검증하면 좋을지 고민
        // given
        Diary diary = Diary.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when
        diaryService.delete(memberA.getId(), diary.getId());

        // then
        assertThat(diaryRepository.findById(diary.getId())).isEmpty();
    }

    @Test
    void 타인의_기록_삭제() {
        // given
        Diary diary = Diary.builder()
                .content("기록 본문")
                .imageUrl("https://plango-backend/imageA.jpg")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when, then
        assertThatThrownBy(() -> diaryService.delete(memberB.getId(), diary.getId()))
                .isInstanceOf(ScheduleAccessDeniedException.class);
        assertThat(diaryRepository.findById(diary.getId())).isPresent();
    }

    @Test
    void 날짜별_기록_목록_조회() {
        // given
        List<Schedule> schedules = List.of(
                Schedule.builder()
                        .title("일정 1")
                        .date(LocalDate.of(2023, 6, 25))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().content("기록 1").build())
                        .build(),
                Schedule.builder()
                        .title("일정 2")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().content("기록 2").build())
                        .build(),
                Schedule.builder()
                        .title("일정 3")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(0, 0, 0))
                        .endTime(LocalTime.of(0, 0, 1))
                        .member(memberA)
                        .diary(Diary.builder().content("기록 3").build())
                        .build(),
                Schedule.builder()
                        .title("일정 4")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(12, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().content("기록 4").build())
                        .build(),
                Schedule.builder()
                        .title("일정 5")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .diary(Diary.builder().content("기록 5").build())
                        .build(),
                Schedule.builder()
                        .title("일정 6")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(23, 59, 59))
                        .endTime(LocalTime.of(0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 7")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(10, 0, 0))
                        .endTime(LocalTime.of(11, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().content("기록 7").build())
                        .build(),
                Schedule.builder()
                        .title("일정 8")
                        .date(LocalDate.of(2023, 6, 26))
                        .startTime(LocalTime.of(15, 0, 0))
                        .endTime(LocalTime.of(22, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().content("기록 8").build())
                        .build()
        );
        scheduleRepository.saveAll(schedules);

        // when
        List<DiaryListResponseDto> responseDtos = diaryService.findAllByDate(memberA.getId(), LocalDate.of(2023, 6, 26));

        // then
        List<String> expectedContents = List.of("기록 2", "기록 3", "기록 4", "기록 5");
        assertThat(responseDtos)
                .extracting(DiaryListResponseDto::getContent)
                .isEqualTo(expectedContents);
    }
}
