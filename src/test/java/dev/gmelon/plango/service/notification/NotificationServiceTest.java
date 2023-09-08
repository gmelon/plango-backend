package dev.gmelon.plango.service.notification;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.notification.NotificationAccessDeniedException;
import dev.gmelon.plango.service.notification.dto.NotificationResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.stream.IntStream;

import static dev.gmelon.plango.domain.notification.NotificationType.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class NotificationServiceTest {

    private Member memberA;
    private Member memberB;

    private Schedule scheduleA;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ScheduleMemberRepository scheduleMemberRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationService notificationService;

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

        scheduleA = Schedule.builder()
                .title("일정 제목")
                .build();
        scheduleRepository.save(scheduleA);
        scheduleMemberRepository.save(ScheduleMember.createOwner(memberA, scheduleA));
        scheduleMemberRepository.save(ScheduleMember.createParticipant(memberB, scheduleA));
    }

    @Test
    void 알림_목록_페이징_조회() {
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
    void 알림_단건_삭제() {
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
    void 타인의_알림_삭제시_예외_발생() {
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
    void 알림_전체_삭제() {
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
    void 일정_참가자에게_일정_초대_알림_발송() {
        // when
        notificationService.sendScheduleInvited(memberB.getId(), scheduleA.getId());

        // then
        Notification expectedNotification = Notification.builder()
                .title(String.format(SCHEDULE_INVITED.getTitleMessageFormat(), scheduleA.getTitle()))
                .content(String.format(SCHEDULE_INVITED.getContentMessageFormat()))
                .argument(scheduleA.getId().toString())
                .notificationType(SCHEDULE_INVITED)
                .build();

        Notification foundNotification = notificationRepository.findAll().get(0);
        assertNotificationIsEqualsTo(foundNotification, expectedNotification, memberB.getId());
    }

    @Test
    void 일정_참가자가_수락시_생성자에게_알림_발송() {
        // when
        notificationService.sendScheduleAccepted(memberA.getId(), scheduleA.getId(), memberB.getId());

        // then
        Notification expectedNotification = Notification.builder()
                .title(String.format(SCHEDULE_ACCEPTED.getTitleMessageFormat(), scheduleA.getTitle()))
                .content(String.format(SCHEDULE_ACCEPTED.getContentMessageFormat(), memberB.getNickname()))
                .argument(scheduleA.getId().toString())
                .notificationType(SCHEDULE_ACCEPTED)
                .build();

        Notification foundNotification = notificationRepository.findAll().get(0);
        assertNotificationIsEqualsTo(foundNotification, expectedNotification, memberA.getId());
    }

    @Test
    void 일정_생성자에게_일정_참가자의_일정_탈퇴_알림_발송() {
        // when
        notificationService.sendScheduleExitedByParticipant(memberA.getId(), scheduleA.getId(), memberB.getId());

        // then
        Notification expectedNotification = Notification.builder()
                .title(String.format(SCHEDULE_EXITED_BY_PARTICIPANT.getTitleMessageFormat(), scheduleA.getTitle()))
                .content(String.format(SCHEDULE_EXITED_BY_PARTICIPANT.getContentMessageFormat(), memberB.getNickname()))
                .argument(scheduleA.getId().toString())
                .notificationType(SCHEDULE_EXITED_BY_PARTICIPANT)
                .build();

        Notification foundNotification = notificationRepository.findAll().get(0);
        assertNotificationIsEqualsTo(foundNotification, expectedNotification, memberA.getId());
    }

    @Test
    void 일정_참가자에게_일정_탈퇴_알림_발송() {
        // when
        notificationService.sendScheduleExitedByOwner(memberB.getId(), scheduleA.getId());

        // then
        Notification expectedNotification = Notification.builder()
                .title(String.format(SCHEDULE_EXITED_BY_OWNER.getTitleMessageFormat(), scheduleA.getTitle()))
                .content(String.format(SCHEDULE_EXITED_BY_OWNER.getContentMessageFormat()))
                .notificationType(SCHEDULE_EXITED_BY_OWNER)
                .build();

        Notification foundNotification = notificationRepository.findAll().get(0);
        assertNotificationIsEqualsTo(foundNotification, expectedNotification, memberB.getId());
    }

    @Test
    void 일정_참가자들에게_일정_수정_알림_발송() {
        // when
        notificationService.sendScheduleEdited(scheduleA.getId());

        // then
        Notification expectedNotification = Notification.builder()
                .title(String.format(SCHEDULE_EDITED.getTitleMessageFormat(), scheduleA.getTitle()))
                .content(String.format(SCHEDULE_EDITED.getContentMessageFormat()))
                .argument(scheduleA.getId().toString())
                .notificationType(SCHEDULE_EDITED)
                .build();
        List<Long> expectedMemberIds = List.of(memberA.getId(), memberB.getId());

        List<Notification> foundNotifications = notificationRepository.findAll();
        for (int i = 0, foundNotificationsSize = foundNotifications.size(); i < foundNotificationsSize; i++) {
            assertNotificationIsEqualsTo(foundNotifications.get(i), expectedNotification, expectedMemberIds.get(i));
        }
    }

    @Test
    void 일정_참가자들에게_일정_삭제_알림_발송() {
        // when
        notificationService.sendScheduleDeleted(scheduleA.getId());

        // then
        Notification expectedNotification = Notification.builder()
                .title(String.format(SCHEDULE_DELETED.getTitleMessageFormat(), scheduleA.getTitle()))
                .content(String.format(SCHEDULE_DELETED.getContentMessageFormat()))
                .notificationType(SCHEDULE_DELETED)
                .build();
        List<Long> expectedMemberIds = List.of(memberA.getId(), memberB.getId());

        List<Notification> foundNotifications = notificationRepository.findAll();
        for (int i = 0, foundNotificationsSize = foundNotifications.size(); i < foundNotificationsSize; i++) {
            assertNotificationIsEqualsTo(foundNotifications.get(i), expectedNotification, expectedMemberIds.get(i));
        }
    }

    private void assertNotificationIsEqualsTo(Notification foundNotification, Notification expectedNotification, Long expectedReceivedMemberId) {
        assertThat(foundNotification.memberId()).isEqualTo(expectedReceivedMemberId);

        assertThat(foundNotification)
                .usingRecursiveComparison()
                .ignoringFields("id", "member", "createdTime", "modifiedDate")
                .isEqualTo(expectedNotification);
    }
}
