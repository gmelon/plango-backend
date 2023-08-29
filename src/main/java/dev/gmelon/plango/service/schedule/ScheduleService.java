package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleEditor;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.schedule.NoSuchScheduleException;
import dev.gmelon.plango.exception.schedule.ScheduleAccessDeniedException;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.schedule.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
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

        return ScheduleResponseDto.from(schedule, isDiaryPresent(memberId, scheduleId));
    }

    public List<ScheduleListResponseDto> findAllByDate(Long memberId, LocalDate requestDate, boolean noDiaryOnly) {
        List<Schedule> schedules = scheduleRepository.findAllByMemberIdAndDate(memberId, requestDate);
        if (noDiaryOnly) {
            schedules = filterDoNotHaveDiaryOnly(memberId, schedules);
        }

        return schedules.stream()
                .map(ScheduleListResponseDto::from)
                .collect(toList());
    }

    private List<Schedule> filterDoNotHaveDiaryOnly(Long memberId, List<Schedule> schedules) {
        return schedules.stream()
                .filter(schedule -> !isDiaryPresent(memberId, schedule.getId()))
                .collect(toList());
    }

    private boolean isDiaryPresent(Long memberId, Long scheduleId) {
        return diaryRepository.findByMemberIdAndScheduleId(memberId, scheduleId).isPresent();
    }

    @Transactional
    public void edit(Long memberId, Long scheduleId, ScheduleEditRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);

        ScheduleEditor scheduleEditor = requestDto.toEditor();
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

        deleteAllDiaryImages(schedule);
        scheduleRepository.delete(schedule);
    }

    private void deleteAllDiaryImages(Schedule schedule) {
        List<String> diaryImageUrls = findDiaryImageUrlsByScheduleId(schedule.getId());
        s3Repository.deleteAll(diaryImageUrls);
    }

    private List<String> findDiaryImageUrlsByScheduleId(Long scheduleId) {
        return diaryRepository.findAllByScheduleId(scheduleId).stream()
                .flatMap(diary -> diary.getDiaryImageUrls().stream())
                .collect(toList());
    }

    public List<ScheduleCountResponseDto> getCountByDays(Long memberId, YearMonth requestMonth) {
        LocalDate startDate = requestMonth.atDay(1);
        LocalDate endDate = requestMonth.atEndOfMonth();

        return scheduleRepository.findCountOfDaysByMemberId(memberId, startDate, endDate);
    }

    private void validateMember(Schedule schedule, Member member) {
        if (!schedule.getMember().equals(member)) {
            throw new ScheduleAccessDeniedException();
        }
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
