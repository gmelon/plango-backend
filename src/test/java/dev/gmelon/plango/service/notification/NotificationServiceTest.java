package dev.gmelon.plango.service.notification;

import static dev.gmelon.plango.domain.notification.type.NotificationType.NotificationArguments;
import static dev.gmelon.plango.domain.notification.type.TestNotificationType.SCHEDULE_INVITED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.member.MemberType;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.domain.notification.type.TestNotificationType;
import dev.gmelon.plango.infrastructure.fcm.FirebaseCloudMessageService;
import dev.gmelon.plango.service.notification.dto.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.jdbc.Sql;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class NotificationServiceTest {

    private Member memberA;
    private Member memberB;

    @MockBean
    private FirebaseCloudMessageService firebaseCloudMessageService;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @BeforeEach
    void setUp() {
        memberA = Member.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .build();
        memberRepository.save(memberA);

        memberB = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .build();
        memberRepository.save(memberB);
    }

    @Test
    void 알림을_생성하고_FirebaseCloudMessage를_발송한다() {
        // given
        NotificationArguments notificationArguments = NotificationArguments.builderOf(TestNotificationType.SCHEDULE_INVITED)
                .titleArgument("한강 산책")
                .contentArgument("회원 A")
                .contentArgument("회원 B")
                .notificationArgument("1")
                .build();

        // when
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .targetMemberId(memberA.getId())
                .notificationType(SCHEDULE_INVITED)
                .notificationArguments(notificationArguments)
                .build();
        notificationService.send(notificationEvent);

        // then
        Notification foundNotification = notificationRepository.findAll().get(0);
        Notification expectedNotification = Notification.builder()
                .title("일정 제목 - 한강 산책")
                .content("회원 A님이 회원 B님을 일정에 초대했습니다.")
                .notificationType(TestNotificationType.SCHEDULE_INVITED)
                .argument("1")
                .member(memberA)
                .build();

        assertThat(foundNotification)
                .usingRecursiveComparison()
                .ignoringFields("id", "modifiedDate", "createdTime", "member")
                .isEqualTo(expectedNotification);
        assertThat(foundNotification.memberId()).isEqualTo(memberA.getId());

        verify(firebaseCloudMessageService).sendMessageTo(foundNotification, memberA);
    }

}
