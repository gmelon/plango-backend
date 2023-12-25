package dev.gmelon.plango.service.notification;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.domain.notification.type.NotificationType;
import dev.gmelon.plango.domain.notification.type.NotificationType.NotificationArguments;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.notification.NoSuchNotificationException;
import dev.gmelon.plango.exception.notification.NotificationAccessDeniedException;
import dev.gmelon.plango.infrastructure.fcm.FirebaseCloudMessageService;
import dev.gmelon.plango.service.notification.dto.NotificationEvent;
import dev.gmelon.plango.service.notification.dto.NotificationResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

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
        notificationRepository.deleteAllInBatchByMemberId(memberId);
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(NotificationEvent event) {
        Member targetMember = findMemberById(event.getTargetMemberId());

        Notification notification = buildNotification(event, targetMember);
        notificationRepository.save(notification);

        firebaseCloudMessageService.sendMessageTo(notification, targetMember);
    }

    private Notification buildNotification(NotificationEvent event, Member targetMember) {
        NotificationType notificationType = event.getNotificationType();
        NotificationArguments notificationArguments = event.getNotificationArguments();

        return Notification.builder()
                .notificationType(notificationType)
                .argument(notificationArguments.getNotificationArgument())
                .member(targetMember)
                .title(notificationType.formatTitle(notificationArguments.getTitleArguments()))
                .content(notificationType.formatContent(notificationArguments.getContentArguments()))
                .build();
    }

    private Member findMemberById(Long receiverMemberId) {
        return memberRepository.findById(receiverMemberId)
                .orElseThrow(NoSuchMemberException::new);
    }

}
