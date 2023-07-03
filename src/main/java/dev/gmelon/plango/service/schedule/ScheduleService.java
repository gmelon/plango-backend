package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleEditor;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.UnauthorizedException;
import dev.gmelon.plango.service.schedule.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(Long memberId, ScheduleCreateRequestDto requestDto) {
        Member member = findMemberById(memberId);

        // TODO request에서 startTime < endTime 인지 검증!!

        Schedule requestSchedule = requestDto.toEntity(member);

        Schedule savedSchedule = scheduleRepository.save(requestSchedule);
        return savedSchedule.getId();
    }

    public ScheduleResponseDto findById(Long memberId, Long scheduleId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);

        return ScheduleResponseDto.of(schedule);
    }

    public List<ScheduleListResponseDto> findAllByDate(Long memberId, LocalDate requestDate, boolean hasDiary) {
        List<Schedule> schedules = findSchedulesByMemberAndDate(memberId, requestDate, hasDiary);

        return schedules.stream()
                .map(ScheduleListResponseDto::of)
                .collect(Collectors.toList());
    }

    private List<Schedule> findSchedulesByMemberAndDate(Long memberId, LocalDate requestDate, boolean hasDiary) {
        LocalDateTime startDateTime = LocalDateTime.of(requestDate, LocalTime.of(0, 0, 0));
        LocalDateTime endDateTime = LocalDateTime.of(requestDate, LocalTime.of(23, 59, 59));

        if (hasDiary) {
            return scheduleRepository.findByMemberIdAndStartTimeBetweenAndDiaryNotNullOrderByStartTimeAscEndTimeAsc(memberId, startDateTime, endDateTime);
        }
        return scheduleRepository.findByMemberIdAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(memberId, startDateTime, endDateTime);
    }

    @Transactional
    public void edit(Long memberId, Long scheduleId, ScheduleEditRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);

        ScheduleEditor scheduleEditor = requestDto.toScheduleEditor();
        schedule.edit(scheduleEditor);
    }

    @Transactional
    public void editDone(Long memberId, Long scheduleId, ScheduleEditDoneRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);

        schedule.changeDone(requestDto.getIsDone());
    }

    @Transactional
    public void delete(Long memberId, Long scheduleId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);

        scheduleRepository.delete(schedule);
    }

    public List<ScheduleCountResponseDto> getCountOfDaysInMonth(Long memberId, YearMonth requestMonth) {
        LocalDate startDate = LocalDate.of(requestMonth.getYear(), requestMonth.getMonth(), 1);
        LocalDate endDate = LocalDate.of(requestMonth.getYear(), requestMonth.getMonth(), requestMonth.lengthOfMonth());

        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.of(0, 0, 0));
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.of(23, 59, 59));

        List<Schedule> schedules = scheduleRepository.findByMemberIdAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(memberId, startDateTime, endDateTime);

        // TODO DB단에서 처리할 수 있도록 리팩토링 하기
        return schedules.stream()
                .collect(Collectors.groupingBy((Schedule schedule) -> schedule.getStartTime().toLocalDate(), Collectors.counting()))
                .entrySet().stream()
                .map(entry -> ScheduleCountResponseDto.builder()
                        .date(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .sorted()
                .collect(Collectors.toList());
    }

    private void validateMember(Schedule schedule, Member member) {
        if (!schedule.getMember().equals(member)) {
            throw new UnauthorizedException();
        }
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계획입니다."));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }
}
