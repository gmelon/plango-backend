package dev.gmelon.plango.domain.notification.dto;

import dev.gmelon.plango.domain.notification.entity.Notification;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class NotificationResponseDto {

    private Long id;

    private String title;

    private String content;

    private String notificationType;

    private String argument;

    @Builder
    public NotificationResponseDto(Long id, String title, String content, String notificationType, String argument) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
        this.argument = argument;
    }

    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .notificationType(notification.getNotificationType().toString())
                .argument(notification.getArgument())
                .build();
    }

}
