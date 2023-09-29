package dev.gmelon.plango.web.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.service.notification.dto.NotificationResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.IntStream;

import static dev.gmelon.plango.domain.notification.type.DefaultNotificationType.SCHEDULE_INVITED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class NotificationControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @PlangoMockUser
    @Test
    void 알림_목록_페이징_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        List<Notification> notifications = IntStream.rangeClosed(0, 60)
                .mapToObj(index -> Notification.builder()
                        .title("알림 제목 " + index)
                        .notificationType(SCHEDULE_INVITED)
                        .member(member)
                        .build())
                .collect(toList());
        notificationRepository.saveAll(notifications);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/notifications"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        NotificationResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), NotificationResponseDto[].class);

        assertThat(responseDtos).hasSize(40);
        assertThat(responseDtos[0].getTitle()).isEqualTo("알림 제목 60");
        assertThat(responseDtos[39].getTitle()).isEqualTo("알림 제목 21");
    }

    @PlangoMockUser
    @Test
    void 알림_단건_삭제() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Notification givenNotification = Notification.builder()
                .title("알림 제목")
                .member(member)
                .build();
        notificationRepository.save(givenNotification);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/notifications/{notificationId}", givenNotification.getId()))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        assertThat(notificationRepository.findById(givenNotification.getId())).isEmpty();
    }

    @PlangoMockUser
    @Test
    void 타인의_알림_삭제시_예외_발생() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Notification givenNotification = Notification.builder()
                .title("알림 제목")
                .member(anotherMember)
                .build();
        notificationRepository.save(givenNotification);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/notifications/{notificationId}", givenNotification.getId()))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        assertThat(notificationRepository.findById(givenNotification.getId())).isPresent();
    }

    @PlangoMockUser
    @Test
    void 알림_전체_삭제() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        List<Notification> givenNotifications = List.of(
                Notification.builder()
                        .title("알림 제목 1")
                        .member(member)
                        .build(),
                Notification.builder()
                        .title("알림 제목 2")
                        .member(member)
                        .build(),
                Notification.builder()
                        .title("알림 제목 3")
                        .member(anotherMember)
                        .build()
        );
        notificationRepository.saveAll(givenNotifications);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/notifications"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        assertThat(notificationRepository.findAll()).hasSize(1);
        assertThat(notificationRepository.findAll().get(0).getMember().getId()).isEqualTo(anotherMember.getId());
    }

    private Member createAnotherMember() {
        Member member = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .build();
        return memberRepository.save(member);
    }
}
