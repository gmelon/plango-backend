package dev.gmelon.plango.service.diary;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.UnauthorizedException;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
                .name("nameA")
                .build();
        memberRepository.save(memberA);

        memberB = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .name("nameB")
                .build();
        memberRepository.save(memberB);

        scheduleOfMemberA = Schedule.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 25, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 25, 11, 0, 0))
                .member(memberA)
                .build();
        scheduleRepository.save(scheduleOfMemberA);
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
        Long createdDiaryId = diaryService.create(memberA.getId(), scheduleOfMemberA.getId(), request);

        // then
        assertThat(createdDiaryId).isNotNull();

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
        assertThatThrownBy(() -> diaryService.create(memberB.getId(), scheduleOfMemberA.getId(), request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 기록_단건_조회() {
        // given
        Diary diary = Diary.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when
        DiaryResponseDto responseDto = diaryService.findById(memberA.getId(), diary.getId());

        // then
        assertThat(responseDto.getId()).isEqualTo(diary.getId());
        assertThat(responseDto.getTitle()).isEqualTo(diary.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(diary.getContent());
        assertThat(responseDto.getImageUrl()).isEqualTo(diary.getImageUrl());

        assertThat(responseDto.getSchedule().getTitle()).isEqualTo(scheduleOfMemberA.getTitle());
        assertThat(responseDto.getSchedule().getStartTime()).isEqualTo(scheduleOfMemberA.getStartTime());
        assertThat(responseDto.getSchedule().getEndTime()).isEqualTo(scheduleOfMemberA.getEndTime());
    }

    @Test
    void 타인의_기록_단건_조회() {
        // given
        Diary diary = Diary.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when, then
        assertThatThrownBy(() -> diaryService.findById(memberB.getId(), diary.getId()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 존재하지_않는_기록_단건_조회() {
        // when, then
        assertThatThrownBy(() -> diaryService.findById(memberB.getId(), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 기록입니다.");
    }

    @Test
    void 기록_수정() {
        // given
        Diary diary = Diary.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        DiaryEditRequestDto requestDto = DiaryEditRequestDto.builder()
                .title("기록 제목 B")
                .content("기록 본문 B")
                .imageUrl("https://image.com/imageB")
                .build();

        // when
        diaryService.edit(memberA.getId(), diary.getId(), requestDto);

        // then
        Diary foundDiary = assertDoesNotThrow(() -> diaryRepository.findById(diary.getId()).get());

        assertThat(foundDiary.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(foundDiary.getContent()).isEqualTo(requestDto.getContent());
        assertThat(foundDiary.getImageUrl()).isEqualTo(requestDto.getImageUrl());
    }

    @Test
    void 타인의_기록_수정() {
        // given
        Diary diary = Diary.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        DiaryEditRequestDto requestDto = DiaryEditRequestDto.builder()
                .title("기록 제목 B")
                .content("기록 본문 B")
                .imageUrl("https://image.com/imageB")
                .build();

        // when, then
        assertThatThrownBy(() -> diaryService.edit(memberB.getId(), diary.getId(), requestDto))
                .isInstanceOf(UnauthorizedException.class);

        Diary foundDiary = assertDoesNotThrow(() -> diaryRepository.findById(diary.getId()).get());
        assertThat(foundDiary)
                .usingRecursiveComparison()
                .isEqualTo(diary);
    }

    @Test
    void 기록_삭제() {
        // given
        Diary diary = Diary.builder()
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
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
                .title("기록 제목")
                .content("기록 본문")
                .imageUrl("https://image.com/imageA")
                .build();
        diaryRepository.save(diary);

        Schedule schedule = scheduleRepository.findById(scheduleOfMemberA.getId()).get();
        schedule.addDiary(diary);
        scheduleRepository.save(schedule);

        // when, then
        assertThatThrownBy(() -> diaryService.delete(memberB.getId(), diary.getId()))
                .isInstanceOf(UnauthorizedException.class);
        assertThat(diaryRepository.findById(diary.getId())).isPresent();
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
                        .startTime(LocalDateTime.of(2023, 6, 26, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 27, 0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 7")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                        .member(memberB)
                        .diary(Diary.builder().title("기록 7").build())
                        .build(),
                Schedule.builder()
                        .title("계획 8")
                        .startTime(LocalDateTime.of(2023, 6, 26, 15, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 22, 0, 1))
                        .member(memberB)
                        .diary(Diary.builder().title("기록 8").build())
                        .build()
        );
        scheduleRepository.saveAll(schedules);

        // when
        List<DiaryListResponseDto> responseDtos = diaryService.findAllByDate(memberA.getId(), LocalDate.of(2023, 6, 26));

        // then
        List<Integer> expectedTitleIndex = List.of(2, 3, 4, 5);
        assertThat(responseDtos).extracting(DiaryListResponseDto::getTitle)
                .isEqualTo(expectedTitleIndex.stream().map(index -> "기록 " + index).collect(Collectors.toList()));
        assertThat(responseDtos).extracting(responseDto -> responseDto.getSchedule().getTitle())
                .isEqualTo(expectedTitleIndex.stream().map(index -> "계획 " + index).collect(Collectors.toList()));
    }
}
