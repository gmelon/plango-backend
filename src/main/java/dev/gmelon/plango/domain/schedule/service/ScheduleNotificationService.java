package dev.gmelon.plango.domain.schedule.service;

import static dev.gmelon.plango.domain.notification.entity.DefaultNotificationType.NotificationArguments;
import static dev.gmelon.plango.domain.notification.entity.DefaultNotificationType.SCHEDULE_ACCEPTED;
import static dev.gmelon.plango.domain.notification.entity.DefaultNotificationType.SCHEDULE_DELETED;
import static dev.gmelon.plango.domain.notification.entity.DefaultNotificationType.SCHEDULE_EDITED;
import static dev.gmelon.plango.domain.notification.entity.DefaultNotificationType.SCHEDULE_EXITED_BY_OWNER;
import static dev.gmelon.plango.domain.notification.entity.DefaultNotificationType.SCHEDULE_EXITED_BY_PARTICIPANT;
import static dev.gmelon.plango.domain.notification.entity.DefaultNotificationType.SCHEDULE_INVITED;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.exception.NoSuchMemberException;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.notification.dto.NotificationEvent;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.schedule.entity.ScheduleMember;
import dev.gmelon.plango.domain.schedule.exception.NoSuchScheduleException;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ScheduleNotificationService {
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void sendInvitedNotifications(Schedule schedule, List<ScheduleMember> scheduleMembers) {
        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_INVITED)
                .titleArgument(schedule.getTitle())
                .notificationArgument(schedule.getId().toString())
                .build();

        for (ScheduleMember scheduleMember : scheduleMembers) {
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .targetMemberId(scheduleMember.memberId())
                    .notificationType(SCHEDULE_INVITED)
                    .notificationArguments(notificationArguments)
                    .build();
            eventPublisher.publishEvent(notificationEvent);
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
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .targetMemberId(scheduleMember.memberId())
                    .notificationType(SCHEDULE_EDITED)
                    .notificationArguments(notificationArguments)
                    .build();
            eventPublisher.publishEvent(notificationEvent);
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
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .targetMemberId(scheduleMember.memberId())
                    .notificationType(SCHEDULE_DELETED)
                    .notificationArguments(notificationArguments)
                    .build();
            eventPublisher.publishEvent(notificationEvent);
        }
    }

    public void sendInvitedNotification(Schedule schedule, ScheduleMember scheduleMember) {
        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_INVITED)
                .titleArgument(schedule.getTitle())
                .notificationArgument(schedule.getId().toString())
                .build();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .targetMemberId(scheduleMember.memberId())
                .notificationType(SCHEDULE_INVITED)
                .notificationArguments(notificationArguments)
                .build();
        eventPublisher.publishEvent(notificationEvent);
    }

    public void sendAcceptedNotification(Long scheduleId, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);
        Member participant = findMemberById(memberId);

        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_ACCEPTED)
                .titleArgument(schedule.getTitle())
                .contentArgument(participant.getNickname())
                .notificationArgument(schedule.getId().toString())
                .build();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .targetMemberId(schedule.ownerId())
                .notificationType(SCHEDULE_ACCEPTED)
                .notificationArguments(notificationArguments)
                .build();
        eventPublisher.publishEvent(notificationEvent);
    }

    public void sendRemovedNotification(Schedule schedule, Long targetMemberId) {
        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_EXITED_BY_OWNER)
                .titleArgument(schedule.getTitle())
                .build();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .targetMemberId(targetMemberId)
                .notificationType(SCHEDULE_EXITED_BY_OWNER)
                .notificationArguments(notificationArguments)
                .build();
        eventPublisher.publishEvent(notificationEvent);
    }

    public void sendRejectedOrExitedNotification(Schedule schedule, Long memberId) {
        Member participant = findMemberById(memberId);

        NotificationArguments notificationArguments = NotificationArguments.builderOf(SCHEDULE_EXITED_BY_PARTICIPANT)
                .titleArgument(schedule.getTitle())
                .contentArgument(participant.getNickname())
                .notificationArgument(schedule.getId().toString())
                .build();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .targetMemberId(schedule.ownerId())
                .notificationType(SCHEDULE_EXITED_BY_PARTICIPANT)
                .notificationArguments(notificationArguments)
                .build();
        eventPublisher.publishEvent(notificationEvent);
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findByIdWithMembers(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
