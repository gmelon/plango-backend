package dev.gmelon.plango.domain.notification.entity;

import static java.lang.String.format;

import dev.gmelon.plango.domain.notification.exception.IllegalNotificationArgumentCountException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public interface NotificationType {

    String getTitleMessageFormat();

    String getContentMessageFormat();

    int getTitleArgsCount();

    int getContentArgsCount();

    default String formatTitle(List<String> titleArguments) {
        return format(getTitleMessageFormat(), titleArguments.toArray());
    }

    default String formatContent(List<String> contentArguments) {
        return format(getContentMessageFormat(), contentArguments.toArray());
    }

    @Getter
    class NotificationArguments {

        private final List<String> titleArguments;
        private final List<String> contentArguments;
        private final String notificationArgument;

        private NotificationArguments(List<String> titleArguments, List<String> contentArguments, String notificationArgument) {
            this.titleArguments = titleArguments;
            this.contentArguments = contentArguments;
            this.notificationArgument = notificationArgument;
        }

        public static NotificationArgumentsBuilder builderOf(NotificationType notificationType) {
            return new NotificationArgumentsBuilder(notificationType);
        }

        public static class NotificationArgumentsBuilder {

            private final NotificationType notificationType;

            private final List<String> titleArguments = new ArrayList<>();
            private final List<String> contentArguments = new ArrayList<>();
            private String notificationArgument;

            public NotificationArgumentsBuilder(NotificationType notificationType) {
                this.notificationType = notificationType;
            }

            public NotificationArgumentsBuilder titleArgument(String argument) {
                titleArguments.add(argument);
                return this;
            }

            public NotificationArgumentsBuilder contentArgument(String argument) {
                contentArguments.add(argument);
                return this;
            }

            public NotificationArgumentsBuilder notificationArgument(String notificationArgument) {
                this.notificationArgument = notificationArgument;
                return this;
            }

            public NotificationArguments build() {
                validateTitleArgsCount();
                validateContentArgsCount();

                return new NotificationArguments(titleArguments, contentArguments, notificationArgument);
            }

            private void validateTitleArgsCount() {
                if (notificationType.getTitleArgsCount() != titleArguments.size()) {
                    throw new IllegalNotificationArgumentCountException(notificationType.getTitleArgsCount(), titleArguments.size());
                }
            }

            private void validateContentArgsCount() {
                if (notificationType.getContentArgsCount() != contentArguments.size()) {
                    throw new IllegalNotificationArgumentCountException(notificationType.getContentArgsCount(), contentArguments.size());
                }
            }

        }

    }

}
