package dev.gmelon.plango.service.notification;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageTokenRepository;
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
import dev.gmelon.plango.service.notification.dto.NotificationResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
    private final FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;

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

    @Transactional
    public void send(Long targetMemberId, NotificationType notificationType, NotificationArguments notificationArguments) {
        Member targetMember = findMemberById(targetMemberId);

        Notification notification = Notification.builder()
                .notificationType(notificationType)
                .argument(notificationArguments.getNotificationArgument())
                .member(targetMember)
                .title(notificationType.formatTitle(notificationArguments.getTitleArguments()))
                .content(notificationType.formatContent(notificationArguments.getContentArguments()))
                .build();
        notificationRepository.save(notification);

        // TODO @Async 적용해서 트랜잭션 분리하기
        try {
            firebaseCloudMessageService.sendMessageTo(notification, targetMember);
        } catch (RuntimeException exception) {
            log.error("FCM 푸시 발송 실패.", exception);
        }
    }

    private Member findMemberById(Long receiverMemberId) {
        return memberRepository.findById(receiverMemberId)
                .orElseThrow(NoSuchMemberException::new);
    }

}
