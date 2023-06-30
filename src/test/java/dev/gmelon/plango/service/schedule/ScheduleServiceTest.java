package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.UnauthorizedException;
import dev.gmelon.plango.service.schedule.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class ScheduleServiceTest {

    private Member memberA;
    private Member memberB;

    @Autowired
    private ScheduleService scheduleService;
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
        Long createdScheduleId = scheduleService.create(memberA.getId(), request);

        // then
        Schedule createdSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(createdSchedule.getMember()).isEqualTo(memberA);
        assertThat(createdSchedule.getTitle()).isEqualTo(request.getTitle());
        assertThat(createdSchedule.getContent()).isEqualTo(request.getContent());
        assertThat(createdSchedule.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(createdSchedule.getEndTime()).isEqualTo(request.getEndTime());
    }

    @Test
    void 계획_단건_조회() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when
        ScheduleResponseDto responseDto = scheduleService.findById(memberA.getId(), createdScheduleId);

        // then
        assertThat(responseDto.getId()).isEqualTo(createdScheduleId);
        assertThat(responseDto.getTitle()).isEqualTo(schedule.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(schedule.getContent());
        assertThat(responseDto.getStartTime()).isEqualTo(schedule.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(schedule.getEndTime());
    }

    @Test
    void 존재하지_않는_계획_단건_조회() {
        // when, then
        assertThatThrownBy(() -> scheduleService.findById(memberA.getId(), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 계획입니다.");
    }

    @Test
    void 타인의_계획_단건_조회() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when, then
        assertThatThrownBy(() -> scheduleService.findById(memberB.getId(), createdScheduleId))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 계획_수정() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        ScheduleEditRequestDto editRequet = ScheduleEditRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 본문")
                .startTime(LocalDateTime.of(2024, 7, 27, 11, 0, 0))
                .endTime(LocalDateTime.of(2024, 7, 27, 12, 0, 0))
                .build();

        // when
        scheduleService.edit(memberA.getId(), createdScheduleId, editRequet);

        // then
        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(foundSchedule.getTitle()).isEqualTo(editRequet.getTitle());
        assertThat(foundSchedule.getContent()).isEqualTo(editRequet.getContent());
        assertThat(foundSchedule.getStartTime()).isEqualTo(editRequet.getStartTime());
        assertThat(foundSchedule.getEndTime()).isEqualTo(editRequet.getEndTime());
    }

    @Test
    void 타인의_계획_수정() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        ScheduleEditRequestDto editRequet = ScheduleEditRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 본문")
                .startTime(LocalDateTime.of(2024, 7, 27, 11, 0, 0))
                .endTime(LocalDateTime.of(2024, 7, 27, 12, 0, 0))
                .build();

        // when, then
        assertThatThrownBy(() -> scheduleService.edit(memberB.getId(), createdScheduleId, editRequet))
                .isInstanceOf(UnauthorizedException.class);

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(foundSchedule)
                .usingRecursiveComparison()
                .ignoringFields("member")
                .isEqualTo(schedule);
    }

    @Test
    void 계획_삭제() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when
        scheduleService.delete(memberA.getId(), createdScheduleId);

        // then
        assertThat(scheduleRepository.findById(createdScheduleId)).isEmpty();
    }

    @Test
    void 타인의_계획_삭제() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .content("계획 본문")
                .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when, then
        assertThatThrownBy(() -> scheduleService.delete(memberB.getId(), createdScheduleId))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 날짜별_계획_목록_조회() {
        // given
        // memberA 계획 추가
        List<Schedule> memberARequests = List.of(
                Schedule.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 1))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 25, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 26, 0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 23, 59, 59))
                        .endTime(LocalDateTime.of(2023, 6, 27, 0, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("A의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 12, 0, 0))
                        .member(memberA)
                        .build()
        );
        scheduleRepository.saveAll(memberARequests);

        // memberB 계획 추가
        List<Schedule> memberBRequests = List.of(
                Schedule.builder()
                        .title("B의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 10, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 11, 0, 0))
                        .member(memberB)
                        .build(),
                Schedule.builder()
                        .title("B의 계획")
                        .startTime(LocalDateTime.of(2023, 6, 26, 15, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 26, 22, 0, 1))
                        .member(memberB)
                        .build()
        );
        scheduleRepository.saveAll(memberBRequests);

        LocalDate targetDate = LocalDate.of(2023, 6, 26);

        // when
        List<ScheduleListResponseDto> scheduleListResponseDtos = scheduleService.findAllByDate(memberA.getId(), targetDate);

        // then
        assertThat(scheduleListResponseDtos)
                .flatExtracting(ScheduleListResponseDto::getTitle).doesNotContain("B의 계획");
        assertThat(scheduleListResponseDtos)
                .flatExtracting((ScheduleListResponseDto dto) -> dto.getStartTime().toLocalDate())
                .containsOnly(targetDate);
    }

    @Test
    void 월별로_계획이_존재하는_날짜의_목록_조회() {
        // given
        List<Schedule> requests = List.of(
                Schedule.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 5, 31, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 1, 12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 1, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 1, 12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 1, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 1, 12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 15, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 17, 12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 17, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 6, 17, 12, 0, 0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 제목")
                        .startTime(LocalDateTime.of(2023, 6, 30, 11, 0, 0))
                        .endTime(LocalDateTime.of(2023, 7, 1, 12, 0, 0))
                        .member(memberA)
                        .build()
        );
        scheduleRepository.saveAll(requests);

        // when
        List<ScheduleCountResponseDto> scheduleCountResponseDtos = scheduleService.getCountOfDaysInMonth(memberA.getId(), YearMonth.of(2023, 6));

        // then
        List<ScheduleCountResponseDto> expectedResponseDtos = List.of(
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 1), 2),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 15), 1),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 17), 1),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 30), 1)
        );
        assertThat(scheduleCountResponseDtos)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseDtos);
    }
}
