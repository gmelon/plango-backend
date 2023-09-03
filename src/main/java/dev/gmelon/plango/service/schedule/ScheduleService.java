package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleEditor;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.query.ScheduleQueryRepository;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleCountQueryDto;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleListQueryDto;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.schedule.*;
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
    private final ScheduleQueryRepository scheduleQueryRepository;

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final S3Repository s3Repository;

    @Transactional
    public Long create(Long memberId, ScheduleCreateRequestDto requestDto) {
        Schedule requestSchedule = requestDto.toEntity();

        Member owner = findMemberById(memberId);
        List<ScheduleMember> scheduleMembers = createScheduleMembers(requestDto.getParticipantIds(), requestSchedule, owner);
        requestSchedule.setScheduleMembers(scheduleMembers);

        // TODO participants에게 알림 발송

        Schedule savedSchedule = scheduleRepository.save(requestSchedule);
        return savedSchedule.getId();
    }

    private List<ScheduleMember> createScheduleMembers(List<Long> participantIds, Schedule requestSchedule, Member owner) {
        validateParticipantsAreNotOwner(participantIds, owner.getId());

        List<ScheduleMember> scheduleMembers = participantIds.stream()
                .distinct()
                .map(participantId ->
                        ScheduleMember.createParticipant(findMemberById(participantId), requestSchedule))
                .collect(toList());
        scheduleMembers.add(ScheduleMember.createOwner(owner, requestSchedule));
        return scheduleMembers;
    }

    private void validateParticipantsAreNotOwner(List<Long> participantIds, Long ownerId) {
        participantIds.stream()
                .filter(participantId -> participantId.equals(ownerId))
                .findAny()
                .ifPresent((participantId) -> {
                    throw new ScheduleOwnerParticipantDuplicateException();
                });
    }

    public ScheduleResponseDto findById(Long memberId, Long scheduleId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);

        return ScheduleResponseDto.from(schedule, member, isDiaryPresent(memberId, scheduleId));
    }

    public List<ScheduleListResponseDto> findAllByDate(Long memberId, LocalDate requestDate, boolean noDiaryOnly) {
        List<ScheduleListQueryDto> schedules = scheduleQueryRepository.findAllByMemberIdAndDate(memberId, requestDate);
        if (noDiaryOnly) {
            schedules = filterDoNotHaveDiaryOnly(memberId, schedules);
        }

        return schedules.stream()
                .map(ScheduleListResponseDto::from)
                .collect(toList());
    }

    private List<ScheduleListQueryDto> filterDoNotHaveDiaryOnly(Long memberId, List<ScheduleListQueryDto> schedules) {
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
        validateAccepted(schedule, member);

        // TODO participants에게 알림 발송

        ScheduleEditor scheduleEditor = requestDto.toEditor();
        schedule.edit(scheduleEditor);
    }

    @Transactional
    public void editDone(Long memberId, Long scheduleId, ScheduleEditDoneRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);
        validateAccepted(schedule, member);

        schedule.changeDone(requestDto.getIsDone());
    }

    @Transactional
    public void delete(Long memberId, Long scheduleId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);
        validateOwner(schedule, member);

        // TODO participants에게 알림 발송

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

        List<ScheduleCountQueryDto> countQueryDtos = scheduleQueryRepository.countOfDaysByMemberId(memberId, startDate, endDate);
        return countQueryDtos.stream()
                .map(ScheduleCountResponseDto::from)
                .collect(toList());
    }

    private void validateMember(Schedule schedule, Member member) {
        if (!schedule.isMember(member.getId())) {
            throw new ScheduleAccessDeniedException();
        }
    }

    private void validateAccepted(Schedule schedule, Member member) {
        if (!schedule.isAccepted(member.getId())) {
            throw new ScheduleNotAcceptedException();
        }
    }

    private void validateOwner(Schedule schedule, Member member) {
        if (!schedule.isOwner(member.getId())) {
            throw new NoOwnerOfScheduleException();
        }
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findByIdWithScheduleMembers(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
