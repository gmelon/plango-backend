package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.schedule.NoSuchScheduleException;
import dev.gmelon.plango.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.gmelon.plango.domain.notification.type.DefaultNotificationType.*;

@RequiredArgsConstructor
@Transactional
@Service
public class ScheduleNotificationService {

    private final NotificationService notificationService;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    public void sendInvitedNotifications(Schedule schedule, List<ScheduleMember> scheduleMembers) {
        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_INVITED)
                .titleArgument(schedule.getTitle())
                .notificationArgument(schedule.getId().toString())
                .build();

        for (ScheduleMember scheduleMember : scheduleMembers) {
            notificationService.send(scheduleMember.memberId(), SCHEDULE_INVITED, notificationArguments);
        }
    }

    public void sendEditedNotifications(Schedule schedule, Member editorMember) {
        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_EDITED)
                .titleArgument(schedule.getTitle())
                .contentArgument(editorMember.getNickname())
                .notificationArgument(schedule.getId().toString())
                .build();

        for (ScheduleMember scheduleMember : schedule.getScheduleMembers()) {
            if (scheduleMember.isMemberEquals(editorMember.getId())) {
                continue;
            }
            notificationService.send(scheduleMember.memberId(), SCHEDULE_EDITED, notificationArguments);
        }
    }


    public void sendDeletedNotification(Schedule schedule, Long memberId) {
        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_DELETED)
                .titleArgument(schedule.getTitle())
                .build();

        for (ScheduleMember scheduleMember : schedule.getScheduleMembers()) {
            if (scheduleMember.isMemberEquals(memberId)) {
                continue;
            }
            notificationService.send(scheduleMember.memberId(), SCHEDULE_DELETED, notificationArguments);
        }
    }

    public void sendInvitedNotification(Schedule schedule, ScheduleMember scheduleMember) {
        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_INVITED)
                .titleArgument(schedule.getTitle())
                .notificationArgument(schedule.getId().toString())
                .build();
        notificationService.send(scheduleMember.memberId(), SCHEDULE_INVITED, notificationArguments);
    }

    public void sendAcceptedNotification(Long scheduleId, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);
        Member participant = findMemberById(memberId);

        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_ACCEPTED)
                .titleArgument(schedule.getTitle())
                .contentArgument(participant.getNickname())
                .notificationArgument(schedule.getId().toString())
                .build();
        notificationService.send(schedule.ownerId(), SCHEDULE_ACCEPTED, notificationArguments);
    }

    public void sendRemovedNotification(Schedule schedule, Long targetMemberId) {
        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_EXITED_BY_OWNER)
                .titleArgument(schedule.getTitle())
                .build();
        notificationService.send(targetMemberId, SCHEDULE_EXITED_BY_OWNER, notificationArguments);
    }

    public void sendRejectedOrExitedNotification(Schedule schedule, Long memberId) {
        Member participant = findMemberById(memberId);

        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_EXITED_BY_PARTICIPANT)
                .titleArgument(schedule.getTitle())
                .contentArgument(participant.getNickname())
                .notificationArgument(schedule.getId().toString())
                .build();
        notificationService.send(schedule.ownerId(), SCHEDULE_EXITED_BY_PARTICIPANT, notificationArguments);
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
