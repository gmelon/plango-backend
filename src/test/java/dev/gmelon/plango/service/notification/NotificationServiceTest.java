package dev.gmelon.plango.service.notification;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.domain.notification.type.TestNotificationType;
import dev.gmelon.plango.exception.notification.NotificationAccessDeniedException;
import dev.gmelon.plango.infrastructure.fcm.FirebaseCloudMessageService;
import dev.gmelon.plango.service.notification.dto.NotificationResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.stream.IntStream;

import static dev.gmelon.plango.domain.notification.type.DefaultNotificationType.NotificationArguments;
import static dev.gmelon.plango.domain.notification.type.DefaultNotificationType.SCHEDULE_INVITED;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

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

    @BeforeEach
    void setUp() {
        memberA = Member.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberA);

        memberB = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberB);
    }

    @Test
    void 알림_목록을_페이징하여_조회한다() {
        // given
        List<Notification> notifications = IntStream.rangeClosed(0, 60)
                .mapToObj(index -> Notification.builder()
                        .title("알림 제목 " + index)
                        .notificationType(SCHEDULE_INVITED)
                        .member(memberA)
                        .build())
                .collect(toList());
        notificationRepository.saveAll(notifications);

        // when
        List<NotificationResponseDto> responses = notificationService.findAll(memberA.getId(), 1);

        // then
        assertThat(responses).hasSize(40);
        assertThat(responses.get(0).getTitle()).isEqualTo("알림 제목 60");
        assertThat(responses.get(39).getTitle()).isEqualTo("알림 제목 21");
    }

    @Test
    void 알림을_하나_삭제한다() {
        // given
        Notification givenNotification = Notification.builder()
                .title("알림 제목")
                .member(memberA)
                .build();
        notificationRepository.save(givenNotification);

        // when
        notificationService.deleteOne(memberA.getId(), givenNotification.getId());

        // then
        assertThat(notificationRepository.findById(givenNotification.getId())).isEmpty();
    }

    @Test
    void 타인의_알림_삭제시_예외가_발생한다() {
        // given
        Notification givenNotification = Notification.builder()
                .title("알림 제목")
                .member(memberB)
                .build();
        notificationRepository.save(givenNotification);

        // when, then
        assertThatThrownBy(() -> notificationService.deleteOne(memberA.getId(), givenNotification.getId()))
                .isInstanceOf(NotificationAccessDeniedException.class);
        assertThat(notificationRepository.findById(givenNotification.getId())).isPresent();
    }

    @Test
    void 알림을_모두_삭제한다() {
        // given
        List<Notification> givenNotifications = List.of(
                Notification.builder()
                        .title("알림 제목 1")
                        .member(memberA)
                        .build(),
                Notification.builder()
                        .title("알림 제목 2")
                        .member(memberA)
                        .build(),
                Notification.builder()
                        .title("알림 제목 3")
                        .member(memberB)
                        .build()
        );
        notificationRepository.saveAll(givenNotifications);

        // when
        notificationService.deleteAll(memberA.getId());

        // then
        assertThat(notificationRepository.findAll()).hasSize(1);
        assertThat(notificationRepository.findAll().get(0).getMember().getId()).isEqualTo(memberB.getId());
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
        notificationService.send(memberA.getId(), TestNotificationType.SCHEDULE_INVITED, notificationArguments);

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
