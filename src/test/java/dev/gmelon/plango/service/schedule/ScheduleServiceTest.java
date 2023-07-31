package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.schedule.NoSuchScheduleException;
import dev.gmelon.plango.exception.schedule.ScheduleAccessDeniedException;
import dev.gmelon.plango.service.schedule.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private DiaryRepository diaryRepository;
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
        Long createdScheduleId = scheduleService.create(memberA.getId(), request);

        // then
        Schedule createdSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(createdSchedule.getMember()).isEqualTo(memberA);
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
    void 기록이_없는_일정_단건_조회() {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .latitude(36.3674097)
                .longitude(127.3454477)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 공과대학 5호관")
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when
        ScheduleResponseDto responseDto = scheduleService.findById(memberA.getId(), createdScheduleId);

        // then
        assertThat(responseDto.getId()).isEqualTo(createdScheduleId);
        assertThat(responseDto.getTitle()).isEqualTo(schedule.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(schedule.getContent());
        assertThat(responseDto.getDate()).isEqualTo(schedule.getDate());
        assertThat(responseDto.getStartTime()).isEqualTo(schedule.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(schedule.getEndTime());
        assertThat(responseDto.getLatitude()).isEqualTo(schedule.getLatitude());
        assertThat(responseDto.getLongitude()).isEqualTo(schedule.getLongitude());
        assertThat(responseDto.getRoadAddress()).isEqualTo(schedule.getRoadAddress());
        assertThat(responseDto.getPlaceName()).isEqualTo(schedule.getPlaceName());
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isFalse();
        assertThat(responseDto.getDiary()).isNull();
    }

    @Test
    void 기록이_있는_일정_단건_조회() {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .latitude(36.3674097)
                .longitude(127.3454477)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 공과대학 5호관")
                .diary(Diary.builder().title("기록 제목").build())
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when
        ScheduleResponseDto responseDto = scheduleService.findById(memberA.getId(), createdScheduleId);

        // then
        assertThat(responseDto.getId()).isEqualTo(createdScheduleId);
        assertThat(responseDto.getTitle()).isEqualTo(schedule.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(schedule.getContent());
        assertThat(responseDto.getDate()).isEqualTo(schedule.getDate());
        assertThat(responseDto.getStartTime()).isEqualTo(schedule.getStartTime());
        assertThat(responseDto.getEndTime()).isEqualTo(schedule.getEndTime());
        assertThat(responseDto.getLatitude()).isEqualTo(schedule.getLatitude());
        assertThat(responseDto.getLongitude()).isEqualTo(schedule.getLongitude());
        assertThat(responseDto.getRoadAddress()).isEqualTo(schedule.getRoadAddress());
        assertThat(responseDto.getPlaceName()).isEqualTo(schedule.getPlaceName());
        assertThat(responseDto.getIsDone()).isFalse();
        assertThat(responseDto.getHasDiary()).isTrue();
        assertThat(responseDto.getDiary().getId()).isNotNull();
    }

    @Test
    void 존재하지_않는_일정_단건_조회() {
        // when, then
        assertThatThrownBy(() -> scheduleService.findById(memberA.getId(), 1L))
                .isInstanceOf(NoSuchScheduleException.class);
    }

    @Test
    void 타인의_일정_단건_조회() {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when, then
        assertThatThrownBy(() -> scheduleService.findById(memberB.getId(), createdScheduleId))
                .isInstanceOf(ScheduleAccessDeniedException.class);
    }

    @Test
    void 일정_수정() {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .latitude(36.3674097)
                .longitude(127.3454477)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 공과대학 5호관")
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

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
        scheduleService.edit(memberA.getId(), createdScheduleId, editRequet);

        // then
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
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        ScheduleEditRequestDto editRequet = ScheduleEditRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 본문")
                .date(LocalDate.of(2024, 7, 27))
                .startTime(LocalTime.of(11, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .build();

        // when, then
        assertThatThrownBy(() -> scheduleService.edit(memberB.getId(), createdScheduleId, editRequet))
                .isInstanceOf(ScheduleAccessDeniedException.class);

        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(foundSchedule)
                .usingRecursiveComparison()
                .ignoringFields("member")
                .isEqualTo(schedule);
    }

    @ParameterizedTest
    @CsvSource(value = {"false:true", "true:false", "false:false", "true:true"}, delimiter = ':')
    void 일정_완료_여부_변경(boolean given, boolean expected) {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .done(given)
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        ScheduleEditDoneRequestDto request = new ScheduleEditDoneRequestDto(expected);

        // when
        scheduleService.editDone(memberA.getId(), createdScheduleId, request);

        // then
        Schedule foundSchedule = assertDoesNotThrow(() -> scheduleRepository.findById(createdScheduleId).get());
        assertThat(foundSchedule.isDone()).isEqualTo(expected);
    }

    @Test
    void 타인의_일정_완료_여부_변경() {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .done(false)
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        ScheduleEditDoneRequestDto request = new ScheduleEditDoneRequestDto(true);

        // when, then
        assertThatThrownBy(() -> scheduleService.editDone(memberB.getId(), createdScheduleId, request))
                .isInstanceOf(ScheduleAccessDeniedException.class);
    }

    @Test
    void 일정_삭제() {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when
        scheduleService.delete(memberA.getId(), createdScheduleId);

        // then
        assertThat(scheduleRepository.findById(createdScheduleId)).isEmpty();
    }

    @Test
    void 타인의_일정_삭제() {
        // given
        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .content("일정 본문")
                .date(LocalDate.of(2023, 6, 26))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(11, 0, 0))
                .member(memberA)
                .build();
        Long createdScheduleId = scheduleRepository.save(schedule).getId();

        // when, then
        assertThatThrownBy(() -> scheduleService.delete(memberB.getId(), createdScheduleId))
                .isInstanceOf(ScheduleAccessDeniedException.class);
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

        LocalDate targetDate = LocalDate.of(2023, 6, 26);

        // when
        List<ScheduleListResponseDto> responseDtos = scheduleService.findAllByDate(memberA.getId(), targetDate, true);

        // then
        List<String> expectedScheduleTitles = List.of("일정 5", "일정 2");

        assertThat(responseDtos)
                .extracting(ScheduleListResponseDto::getTitle).isEqualTo(expectedScheduleTitles);
        assertThat(responseDtos)
                .extracting(ScheduleListResponseDto::getDate)
                .containsOnly(targetDate);
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

        LocalDate targetDate = LocalDate.of(2023, 6, 26);

        // when
        List<ScheduleListResponseDto> responseDtos = scheduleService.findAllByDate(memberA.getId(), targetDate, false);

        // then
        List<String> expectedScheduleTitles = List.of("일정 3", "일정 4", "일정 2", "일정 5");

        assertThat(responseDtos)
                .extracting(ScheduleListResponseDto::getTitle).isEqualTo(expectedScheduleTitles);
        assertThat(responseDtos)
                .extracting(ScheduleListResponseDto::getDate)
                .containsOnly(targetDate);
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
                        .done(true)
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
                        .done(true)
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
        List<ScheduleCountResponseDto> scheduleCountResponseDtos = scheduleService.getCountByDays(memberA.getId(), YearMonth.of(2023, 6));

        // then
        List<ScheduleCountResponseDto> expectedResponseDtos = List.of(
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 1), 1, 2),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 15), 0, 1),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 17), 0, 1),
                new ScheduleCountResponseDto(LocalDate.of(2023, 6, 30), 1, 1)
        );
        assertThat(scheduleCountResponseDtos)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseDtos);
    }
}
