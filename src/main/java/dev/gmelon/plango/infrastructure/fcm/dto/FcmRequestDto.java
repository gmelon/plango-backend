package dev.gmelon.plango.infrastructure.fcm.dto;

import dev.gmelon.plango.domain.notification.Notification;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
public class FcmRequestDto {

    private boolean validate_only;
    private FcmMessage message;

    public static FcmRequestDto from(Notification notification, String targetToken) {
        return FcmRequestDto.builder()
                .validate_only(false)
                .message(FcmMessage.from(notification, targetToken))
                .build();
    }

    @Builder
    @Getter
    public static class FcmMessage {
        private static final String NOTIFICATION_TYPE_KEY = "notificationType";
        private static final String ARGUMENT_KEY = "argument";

        private FcmNotification notification;
        private Map<String, String> data;
        private String token;

        public static FcmMessage from(Notification notification, String targetToken) {
            return FcmMessage.builder()
                    .notification(FcmNotification.from(notification))
                    .data(createData(notification))
                    .token(targetToken)
                    .build();
        }

        private static Map<String, String> createData(Notification notification) {
            Map<String, String> data = new HashMap<>();
            data.put(NOTIFICATION_TYPE_KEY, notification.toString());
            if (notification.getArgument() != null) {
                data.put(ARGUMENT_KEY, notification.getArgument());
            }
            return data;
        }
    }

    @Builder
    @Getter
    public static class FcmNotification {
        private String title;
        private String body;

        public static FcmNotification from(Notification notification) {
            return FcmNotification.builder()
                    .title(notification.getTitle())
                    .body(notification.getContent())
                    .build();
        }
    }

}
