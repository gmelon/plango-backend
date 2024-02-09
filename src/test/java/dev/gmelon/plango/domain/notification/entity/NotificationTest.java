package dev.gmelon.plango.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.notification.entity.type.TestNotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NotificationTest {

    @Test
    void 알림_생성() {
        // given
        String notificationTitle = "알림 제목";
        String notificationContent = "알림 내용";
        String notificationArgument = "알림 인자";
        NotificationType notificationType = TestNotificationType.SCHEDULE_INVITED;
        Member targetMember = Member.builder().id(1L).build();

        // when
        Notification notification = Notification.builder()
                .title(notificationTitle)
                .content(notificationContent)
                .argument(notificationArgument)
                .notificationType(notificationType)
                .member(targetMember)
                .build();

        // then
        assertThat(notification.getTitle()).isEqualTo(notificationTitle);
        assertThat(notification.getContent()).isEqualTo(notificationContent);
        assertThat(notification.getArgument()).isEqualTo(notificationArgument);
        assertThat(notification.getNotificationType()).isEqualTo(TestNotificationType.SCHEDULE_INVITED.toString());
        assertThat(notification.getMember().getId()).isEqualTo(targetMember.getId());
    }

    @Test
    void 알림을_받는_회원의_id를_반환한다() {
        // given
        Member targetMember = Member.builder().id(1L).build();

        // when
        Notification notification = Notification.builder()
                .member(targetMember)
                .build();

        // then
        assertThat(notification.memberId()).isEqualTo(targetMember.getId());
    }

    @ParameterizedTest
    @CsvSource({"1, true", "2, false"})
    void 인자로_전달된_id가_알림을_받는_회원과_일치하는지_확인한다(long memberId, boolean expected) {
        // given
        Member targetMember = Member.builder().id(1L).build();
        Notification notification = Notification.builder()
                .member(targetMember)
                .build();

        // when, then
        assertThat(notification.memberIdEquals(memberId)).isEqualTo(expected);
    }
}
