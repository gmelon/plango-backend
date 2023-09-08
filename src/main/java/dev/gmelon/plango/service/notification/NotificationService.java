package dev.gmelon.plango.service.notification;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.notification.NoSuchNotificationException;
import dev.gmelon.plango.exception.notification.NotificationAccessDeniedException;
import dev.gmelon.plango.exception.schedule.NoSuchScheduleException;
import dev.gmelon.plango.service.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.gmelon.plango.domain.notification.NotificationType.*;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;

    public List<NotificationResponseDto> findAll(Long memberId, int page) {
        return notificationRepository.findAllByMemberId(memberId, page).stream()
                .map(NotificationResponseDto::from)
                .collect(toList());
    }

    @Transactional
    public void deleteOne(Long memberId, Long notificationId) {
        Notification notification = findNotificationById(notificationId);
        validateMember(memberId, notification);

        notificationRepository.deleteById(notificationId);
    }

    private void validateMember(Long memberId, Notification notification) {
        boolean memberIdEquals = notification.memberIdEquals(memberId);
        if (!memberIdEquals) {
            throw new NotificationAccessDeniedException();
        }
    }

    private Notification findNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(NoSuchNotificationException::new);
    }

    @Transactional
    public void deleteAll(Long memberId) {
        notificationRepository.deleteAllByMemberId(memberId);
    }

    // TODO FCMService 구현 후 연동

    @Transactional
    public void sendScheduleInvited(Long receiverMemberId, Long scheduleId) {
        Member receiverMember = findMemberById(receiverMemberId);
        Schedule schedule = findScheduleById(scheduleId);

        Notification notification = Notification.builder()
                .title(SCHEDULE_INVITED.formatTitle(schedule.getTitle()))
                .content(SCHEDULE_INVITED.formatContent())
                .notificationType(SCHEDULE_INVITED)
                .argument(schedule.getId().toString())
                .member(receiverMember)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendScheduleAccepted(Long receiverMemberId, Long scheduleId, Long participantId) {
        Member receiverMember = findMemberById(receiverMemberId);
        Schedule schedule = findScheduleById(scheduleId);
        Member participant = findMemberById(participantId);

        Notification notification = Notification.builder()
                .title(SCHEDULE_ACCEPTED.formatTitle(schedule.getTitle()))
                .content(SCHEDULE_ACCEPTED.formatContent(participant.getNickname()))
                .notificationType(SCHEDULE_ACCEPTED)
                .argument(schedule.getId().toString())
                .member(receiverMember)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendScheduleExitedByParticipant(Long receiverMemberId, Long scheduleId, Long participantId) {
        Member receiverMember = findMemberById(receiverMemberId);
        Schedule schedule = findScheduleById(scheduleId);
        Member participant = findMemberById(participantId);

        Notification notification = Notification.builder()
                .title(SCHEDULE_EXITED_BY_PARTICIPANT.formatTitle(schedule.getTitle()))
                .content(SCHEDULE_EXITED_BY_PARTICIPANT.formatContent(participant.getNickname()))
                .notificationType(SCHEDULE_EXITED_BY_PARTICIPANT)
                .argument(schedule.getId().toString())
                .member(receiverMember)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendScheduleExitedByOwner(Long receiverMemberId, Long scheduleId) {
        Member receiverMember = findMemberById(receiverMemberId);
        Schedule schedule = findScheduleById(scheduleId);

        Notification notification = Notification.builder()
                .title(SCHEDULE_EXITED_BY_OWNER.formatTitle(schedule.getTitle()))
                .content(SCHEDULE_EXITED_BY_OWNER.formatContent())
                .notificationType(SCHEDULE_EXITED_BY_OWNER)
                .member(receiverMember)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendScheduleEdited(Long scheduleId) {
        Schedule schedule = findScheduleById(scheduleId);

        List<Notification> notifications = mapToScheduleEditedNotifications(schedule);
        notificationRepository.saveAll(notifications);
    }

    private List<Notification> mapToScheduleEditedNotifications(Schedule schedule) {
        return schedule.getScheduleMembers().stream()
                .map(scheduleMember -> Notification.builder()
                        .title(SCHEDULE_EDITED.formatTitle(schedule.getTitle()))
                        .content(SCHEDULE_EDITED.formatContent())
                        .notificationType(SCHEDULE_EDITED)
                        .member(scheduleMember.getMember())
                        .argument(schedule.getId().toString())
                        .build())
                .collect(toList());
    }

    @Transactional
    public void sendScheduleDeleted(Long scheduleId) {
        Schedule schedule = findScheduleById(scheduleId);

        List<Notification> notifications = mapToScheduleDeletedNotifications(schedule);
        notificationRepository.saveAll(notifications);
    }

    private List<Notification> mapToScheduleDeletedNotifications(Schedule schedule) {
        return schedule.getScheduleMembers().stream()
                .map(scheduleMember -> Notification.builder()
                        .title(SCHEDULE_DELETED.formatTitle(schedule.getTitle()))
                        .content(SCHEDULE_DELETED.formatContent())
                        .notificationType(SCHEDULE_DELETED)
                        .member(scheduleMember.getMember())
                        .build())
                .collect(toList());
    }

    private Member findMemberById(Long receiverMemberId) {
        return memberRepository.findById(receiverMemberId)
                .orElseThrow(NoSuchMemberException::new);
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findByIdWithScheduleMembers(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

}
