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

    @Transactional
    public void invite(Long scheduleOwnerMemberId, Long scheduleId, ScheduleMemberAddRequestDto requestDto) {
        Schedule schedule = findScheduleById(scheduleId);
        validateOwner(schedule, scheduleOwnerMemberId);

        Long newMemberId = requestDto.getMemberId();
        validateMemberNotExists(schedule, newMemberId);

        Member newMember = findMemberById(newMemberId);
        ScheduleMember newScheduleMember = ScheduleMember.createParticipant(newMember, schedule);
        scheduleMemberRepository.save(newScheduleMember);

        schedule.increaseScheduleMemberCount();

        // TODO 추가된 participant에게 알림 발송
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
        Schedule schedule = findScheduleById(scheduleId);
        validateOwner(schedule, scheduleOwnerMemberId);

        validateMemberExists(schedule, targetMemberId);
        validateTargetMemberNotOwner(scheduleOwnerMemberId, targetMemberId);

        scheduleMemberRepository.deleteByMemberIdAndScheduleId(targetMemberId, schedule.getId());

        schedule.decreaseScheduleMemberCount();

        // TODO 삭제된 participant에게 알림 발송
    }

    private void validateTargetMemberNotOwner(Long memberId, Long targetMemberId) {
        if (targetMemberId.equals(memberId)) {
            throw new DeleteOwnerOfSchduleException();
        }
    }

    private void validateMemberExists(Schedule schedule, Long memberId) {
        if (!schedule.isMember(memberId)) {
            throw new NoSuchScheduleMemberException();
        }
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findByIdWithScheduleMembers(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private void validateOwner(Schedule schedule, Long memberId) {
        if (!schedule.isOwner(memberId)) {
            throw new NoOwnerOfScheduleException();
        }
    }

    @Transactional
    public void acceptInvitation(Long memberId, Long scheduleId) {
        ScheduleMember scheduleMember = findScheduleMemberByMemberIdAndScheduleId(memberId, scheduleId);
        scheduleMember.accept();

        // TODO 일정 owner에게 초대 수락 알림 발송
    }

    @Transactional
    public void rejectOrExitSchedule(Long memberId, Long scheduleId) {
        Schedule schedule = findScheduleById(scheduleId);
        validateMemberNotOwner(memberId, schedule);

        ScheduleMember scheduleMember = findScheduleMemberByMemberIdAndScheduleId(memberId, scheduleId);
        // TODO scheduleMemberRepository.delete(scheduleMember) 로는 왜 삭제 안 되는지
        scheduleMemberRepository.deleteByMemberIdAndScheduleId(memberId, scheduleId);
        schedule.decreaseScheduleMemberCount();
        
        // TODO 일정 owner에게 알림 발송
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
