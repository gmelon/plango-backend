package dev.gmelon.plango.domain.notification.dto;

import dev.gmelon.plango.domain.notification.entity.NotificationType;
import dev.gmelon.plango.domain.notification.entity.NotificationType.NotificationArguments;
import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationEvent {
    private final Long targetMemberId;

    private final NotificationType notificationType;

    private final NotificationArguments notificationArguments;

    @Builder
    public NotificationEvent(Long targetMemberId, NotificationType notificationType,
                             NotificationArguments notificationArguments) {
        this.targetMemberId = targetMemberId;
        this.notificationType = notificationType;
        this.notificationArguments = notificationArguments;
    }
}
