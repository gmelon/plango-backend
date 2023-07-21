package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.config.auth.exception.UnauthorizedException;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleEditor;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.schedule.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final S3Repository s3Repository;

    @Transactional
    public Long create(Long memberId, ScheduleCreateRequestDto requestDto) {
        Member member = findMemberById(memberId);

        Schedule requestSchedule = requestDto.toEntity(member);

        Schedule savedSchedule = scheduleRepository.save(requestSchedule);
        return savedSchedule.getId();
    }

    public ScheduleResponseDto findById(Long memberId, Long scheduleId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);

        return ScheduleResponseDto.from(schedule);
    }

    public List<ScheduleListResponseDto> findAllByDate(Long memberId, LocalDate requestDate, boolean noDiaryOnly) {
        List<Schedule> schedules = findSchedulesByMemberAndDate(memberId, requestDate, noDiaryOnly);

        return schedules.stream()
                .map(ScheduleListResponseDto::from)
                .collect(Collectors.toList());
    }

    private List<Schedule> findSchedulesByMemberAndDate(Long memberId, LocalDate requestDate, boolean noDiaryOnly) {
        if (noDiaryOnly) {
            return scheduleRepository.findByMemberIdAndDateAndDiaryNullOrderByStartTimeAndEndTimeAsc(memberId, requestDate);
        }
        return scheduleRepository.findByMemberIdAndDateOrderByStartTimeAndEndTimeAsc(memberId, requestDate);
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

        Optional<String> diaryImageUrl = getDiaryImageUrl(schedule);
        scheduleRepository.delete(schedule);
        diaryImageUrl.ifPresent(s3Repository::delete);
    }

    private Optional<String> getDiaryImageUrl(Schedule schedule) {
        if (schedule.getDiary() != null) {
            return Optional.ofNullable(schedule.getDiary().getImageUrl());
        }
        return Optional.empty();
    }

    public List<ScheduleCountResponseDto> getCountOfDaysInMonth(Long memberId, YearMonth requestMonth) {
        LocalDate startDate = requestMonth.atDay(1);
        LocalDate endDate = requestMonth.atEndOfMonth();

        List<Schedule> schedules = scheduleRepository.findByMemberIdAndDateBetween(memberId, startDate, endDate);

        // TODO DB단에서 처리할 수 있도록 리팩토링 하기
        return schedules.stream()
                .collect(Collectors.groupingBy(Schedule::getDate, Collectors.counting()))
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
