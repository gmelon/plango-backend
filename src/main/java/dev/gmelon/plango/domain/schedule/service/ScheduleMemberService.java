package dev.gmelon.plango.domain.schedule.service;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.exception.NoSuchMemberException;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.schedule.dto.ScheduleMemberAddRequestDto;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.schedule.entity.ScheduleMember;
import dev.gmelon.plango.domain.schedule.exception.DeleteOwnerOfSchduleException;
import dev.gmelon.plango.domain.schedule.exception.DuplicateScheduleMemberException;
import dev.gmelon.plango.domain.schedule.exception.NoOwnerOfScheduleException;
import dev.gmelon.plango.domain.schedule.exception.NoSuchScheduleException;
import dev.gmelon.plango.domain.schedule.exception.NoSuchScheduleMemberException;
import dev.gmelon.plango.domain.schedule.exception.UnInvitedMemberException;
import dev.gmelon.plango.domain.schedule.repository.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ScheduleMemberService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMemberRepository scheduleMemberRepository;
    private final MemberRepository memberRepository;
    private final ScheduleNotificationService scheduleNotificationService;

    @Transactional
    public void invite(Long scheduleOwnerMemberId, Long scheduleId, ScheduleMemberAddRequestDto requestDto) {
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);
        validateOwner(schedule, scheduleOwnerMemberId);

        Long newMemberId = requestDto.getMemberId();
        validateMemberNotExists(schedule, newMemberId);

        ScheduleMember scheduleMember = createNewScheduleMember(newMemberId, schedule);
        scheduleMemberRepository.save(scheduleMember);

        scheduleNotificationService.sendInvitedNotification(schedule, scheduleMember);
    }

    private ScheduleMember createNewScheduleMember(Long newMemberId, Schedule schedule) {
        Member newMember = findMemberById(newMemberId);
        return ScheduleMember.createParticipant(newMember, schedule);
    }

    private void validateMemberNotExists(Schedule schedule, Long memberId) {
        if (schedule.isMember(memberId)) {
            throw new DuplicateScheduleMemberException();
        }
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }

    @Transactional
    public void remove(Long scheduleOwnerMemberId, Long scheduleId, Long targetMemberId) {
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);
        validateOwner(schedule, scheduleOwnerMemberId);

        validateMemberExists(schedule, targetMemberId);
        validateTargetMemberNotOwner(scheduleOwnerMemberId, targetMemberId);

        ScheduleMember targetScheduleMember = findScheduleMemberByMemberIdAndScheduleId(targetMemberId, scheduleId);
        schedule.deleteScheduleMember(targetScheduleMember);

        scheduleNotificationService.sendRemovedNotification(schedule, targetMemberId);
    }

    private void validateOwner(Schedule schedule, Long memberId) {
        if (!schedule.isOwner(memberId)) {
            throw new NoOwnerOfScheduleException();
        }
    }

    private void validateMemberExists(Schedule schedule, Long memberId) {
        if (!schedule.isMember(memberId)) {
            throw new NoSuchScheduleMemberException();
        }
    }

    private void validateTargetMemberNotOwner(Long memberId, Long targetMemberId) {
        if (targetMemberId.equals(memberId)) {
            throw new DeleteOwnerOfSchduleException();
        }
    }

    @Transactional
    public void acceptInvitation(Long memberId, Long scheduleId) {
        ScheduleMember scheduleMember = findScheduleMemberByMemberIdAndScheduleId(memberId, scheduleId);
        scheduleMember.accept();

        scheduleNotificationService.sendAcceptedNotification(scheduleId, memberId);
    }

    @Transactional
    public void rejectOrExitSchedule(Long memberId, Long scheduleId) {
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);
        validateMemberNotOwner(memberId, schedule);

        ScheduleMember scheduleMember = findScheduleMemberByMemberIdAndScheduleId(memberId, scheduleId);
        schedule.deleteScheduleMember(scheduleMember);

        scheduleNotificationService.sendRejectedOrExitedNotification(schedule, memberId);
    }

    private Schedule findScheduleByIdWithMembers(Long scheduleId) {
        return scheduleRepository.findByIdWithMembers(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private ScheduleMember findScheduleMemberByMemberIdAndScheduleId(Long memberId, Long scheduleId) {
        return scheduleMemberRepository.findByMemberIdAndScheduleId(memberId, scheduleId)
                .orElseThrow(UnInvitedMemberException::new);
    }

    private void validateMemberNotOwner(Long memberId, Schedule schedule) {
        if (schedule.isOwner(memberId)) {
            throw new DeleteOwnerOfSchduleException();
        }
    }
}
