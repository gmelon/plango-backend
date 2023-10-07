package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.schedule.*;
import dev.gmelon.plango.service.schedule.dto.ScheduleMemberAddRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
