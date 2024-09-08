package dev.gmelon.plango.global.infrastructure.fcm;

import dev.gmelon.plango.domain.fcm.repository.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.entity.MemberRole;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class FirebaseCloudMessageServiceTest {

    @MockBean
    private FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;

    @Autowired
    private FirebaseCloudMessageService firebaseCloudMessageService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private Member memberA;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);

        memberA = Member.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberA);
    }

    // TODO accessToken 갱신 테스트 시 의존성 문제 해결
//    @Test
//    void 회원의_모든_tokens로_FirebaseCloudMessage를_발송한다() throws URISyntaxException {
//        // given
//        Notification givenNotification = Notification.builder()
//                .title("알림 제목")
//                .content("알림 본문")
//                .notificationType(DefaultNotificationType.SCHEDULE_INVITED)
//                .argument("1")
//                .member(memberA)
//                .build();
//
//        List<FirebaseCloudMessageToken> memberATokens = List.of(
//                FirebaseCloudMessageToken.builder()
//                        .tokenValue("123-abc")
//                        .build(),
//                FirebaseCloudMessageToken.builder()
//                        .tokenValue("456-def")
//                        .build()
//        );
//        when(firebaseCloudMessageTokenRepository.findAllByMember(memberA)).thenReturn(memberATokens);
//
//        mockServer.expect(ExpectedCount.times(memberATokens.size()),
//                        requestTo(new URI("https://fcm.googleapis.com/v1/projects/plango-395216/messages:send")))
//                .andExpect(method(HttpMethod.POST))
//                .andRespond(
//                        withStatus(HttpStatus.OK)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body("푸시 발송 성공")
//                );
//
//        // when
//        firebaseCloudMessageService.sendMessageTo(givenNotification, memberA);
//
//        // then
//        mockServer.verify();
//    }
}
