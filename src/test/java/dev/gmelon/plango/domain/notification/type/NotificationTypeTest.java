package dev.gmelon.plango.domain.notification.type;

import dev.gmelon.plango.exception.notification.IllegalNotificationArgumentCountException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationTypeTest {

    @Test
    void 전달한_인자들이_title_문자열에_포맷팅된다() {
        // given
        List<String> titleArguments = List.of("한강 산책");

        // when
        String formattedTitle = TestNotificationType.SCHEDULE_INVITED.formatTitle(titleArguments);

        // then
        assertThat(formattedTitle).isEqualTo("일정 제목 - 한강 산책");
    }

    @Test
    void 전달한_인자들이_content_문자열에_포맷팅된다() {
        // given
        List<String> contentArguments = List.of("A", "B");

        // when
        String formattedContent = TestNotificationType.SCHEDULE_INVITED.formatContent(contentArguments);

        // then
        assertThat(formattedContent).isEqualTo("A님이 B님을 일정에 초대했습니다.");
    }

    @Test
    void NotificationArguments를_통해_알림_타입별로_포맷팅_인자를_받을_수_있다() {
        // given
        String titleArgument = "한강 산책";
        String contentArgument1 = "A";
        String contentArgument2 = "B";
        String notificationArgument = "1";

        // when
        NotificationType.NotificationArguments notificationArguments = NotificationType.NotificationArguments.builderOf(TestNotificationType.SCHEDULE_INVITED)
                .titleArgument(titleArgument)
                .contentArgument(contentArgument1)
                .contentArgument(contentArgument2)
                .notificationArgument(notificationArgument)
                .build();

        // then
        assertThat(notificationArguments.getTitleArguments()).isEqualTo(List.of(titleArgument));
        assertThat(notificationArguments.getContentArguments()).isEqualTo(List.of(contentArgument1, contentArgument2));
        assertThat(notificationArguments.getNotificationArgument()).isEqualTo(notificationArgument);
    }

    @Test
    void NotificationArguments에_전달된_titleArgument_개수가_알림_타입과_맞지_않으면_예외가_발생한다() {
        // given
        String contentArgument1 = "A";
        String contentArgument2 = "B";

        // when, then
        assertThatThrownBy(
                () -> NotificationType.NotificationArguments.builderOf(TestNotificationType.SCHEDULE_INVITED)
                        .contentArgument(contentArgument1)
                        .contentArgument(contentArgument2)
                        .build()
        ).isInstanceOf(IllegalNotificationArgumentCountException.class);

        assertThatThrownBy(
                () -> NotificationType.NotificationArguments.builderOf(TestNotificationType.SCHEDULE_INVITED)
                        .titleArgument("")
                        .titleArgument("")
                        .contentArgument(contentArgument1)
                        .contentArgument(contentArgument2)
                        .build()
        ).isInstanceOf(IllegalNotificationArgumentCountException.class);
    }

    @Test
    void NotificationArguments에_전달된_contentArgument_개수가_알림_타입과_맞지_않으면_예외가_발생한다() {
        // given
        String titleArgument = "한강 산책";

        // when, then
        assertThatThrownBy(
                () -> NotificationType.NotificationArguments.builderOf(TestNotificationType.SCHEDULE_INVITED)
                        .titleArgument(titleArgument)
                        .contentArgument("")
                        .build()
        ).isInstanceOf(IllegalNotificationArgumentCountException.class);

        assertThatThrownBy(
                () -> NotificationType.NotificationArguments.builderOf(TestNotificationType.SCHEDULE_INVITED)
                        .titleArgument(titleArgument)
                        .contentArgument("")
                        .contentArgument("")
                        .contentArgument("")
                        .build()
        ).isInstanceOf(IllegalNotificationArgumentCountException.class);
    }
}
