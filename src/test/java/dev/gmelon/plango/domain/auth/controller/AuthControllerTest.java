package dev.gmelon.plango.domain.auth.controller;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.domain.auth.dto.LoginRequestDto;
import dev.gmelon.plango.domain.auth.dto.PasswordResetRequestDto;
import dev.gmelon.plango.domain.auth.dto.SendEmailTokenRequestDto;
import dev.gmelon.plango.domain.auth.dto.SignupRequestDto;
import dev.gmelon.plango.domain.auth.dto.SnsLoginRequestDto;
import dev.gmelon.plango.domain.auth.dto.TokenRefreshRequestDto;
import dev.gmelon.plango.domain.auth.dto.TokenResponseDto;
import dev.gmelon.plango.domain.auth.entity.EmailToken;
import dev.gmelon.plango.domain.auth.repository.EmailTokenRepository;
import dev.gmelon.plango.domain.auth.service.AuthService;
import dev.gmelon.plango.domain.diary.entity.Diary;
import dev.gmelon.plango.domain.diary.repository.DiaryRepository;
import dev.gmelon.plango.domain.fcm.entity.FirebaseCloudMessageToken;
import dev.gmelon.plango.domain.fcm.repository.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.entity.MemberRole;
import dev.gmelon.plango.domain.member.entity.MemberType;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.notification.entity.Notification;
import dev.gmelon.plango.domain.notification.repository.NotificationRepository;
import dev.gmelon.plango.domain.place.entity.PlaceSearchRecord;
import dev.gmelon.plango.domain.place.repository.PlaceSearchRecordRepository;
import dev.gmelon.plango.domain.refreshtoken.repository.RefreshTokenRepository;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.schedule.entity.ScheduleMember;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.repository.query.ScheduleQueryRepository;
import dev.gmelon.plango.global.config.auth.social.SocialClients;
import dev.gmelon.plango.global.config.auth.social.dto.SocialAccountResponse;
import dev.gmelon.plango.global.config.security.PlangoMockUser;
import dev.gmelon.plango.global.dto.ErrorResponseDto;
import dev.gmelon.plango.global.dto.InputInvalidErrorResponseDto;
import dev.gmelon.plango.global.infrastructure.mail.EmailSender;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ScheduleQueryRepository scheduleQueryRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PlaceSearchRecordRepository placeSearchRecordRepository;
    @Autowired
    private FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private EmailTokenRepository emailTokenRepository;

    @MockBean
    private SocialClients socialClients;

    @MockBean
    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        emailTokenRepository.deleteAll();
    }

    @Test
    void 정상_값으로_회원가입() throws Exception {
        // given
        EmailToken emailToken = EmailToken.builder()
                .email("a@a.com")
                .tokenValue("abc123")
                .build();
        emailToken.authenticate();
        emailTokenRepository.save(emailToken);

        SignupRequestDto request = SignupRequestDto.builder()
                .email(emailToken.getEmail())
                .tokenValue(emailToken.getTokenValue())
                .password("passwordA")
                .nickname("nameA")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(memberRepository.findByEmail(request.getEmail())).isPresent();

        Member member = memberRepository.findByEmail(request.getEmail()).get();
        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getNickname()).isEqualTo(request.getNickname());
        assertThat(member.isTermsAccepted()).isTrue();
    }

    @Test
    void 인증되지_않은_메일로_회원가입() throws Exception {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("a@a.com")
                .tokenValue("abc123")
                .password("passwordA")
                .nickname("nameA")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 이미_존재하는_이메일로_회원가입() throws Exception {
        // given
        Member member = createDefaultMember();

        SignupRequestDto secondRequest = SignupRequestDto.builder()
                .email(member.getEmail())
                .tokenValue("abc123")
                .password("passwordA")
                .nickname("nameB")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        InputInvalidErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8),
                InputInvalidErrorResponseDto.class);

        assertThat(responseDto.getField()).isEqualTo("email");
        assertThat(responseDto.getMessage()).isEqualTo("이미 존재하는 이메일입니다.");
    }

    @Test
    void 이미_존재하는_닉네임으로_회원가입() throws Exception {
        // given
        Member member = createDefaultMember();

        SignupRequestDto secondRequest = SignupRequestDto.builder()
                .email("b@b.com")
                .tokenValue("abc123")
                .password("passwordA")
                .nickname(member.getNickname())
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        InputInvalidErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8),
                InputInvalidErrorResponseDto.class);

        assertThat(responseDto.getField()).isEqualTo("nickname");
        assertThat(responseDto.getMessage()).isEqualTo("이미 존재하는 닉네임입니다.");
    }

    @Test
    void 메일_인증_토큰_요청() throws Exception {
        // given
        SendEmailTokenRequestDto request = SendEmailTokenRequestDto.builder()
                .email("a@a.com")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/send-email-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(emailTokenRepository.findById(request.getEmail())).isPresent();
        verify(emailSender).send(any());
    }

    @Test
    void 이미_존재하는_메일에_대한_메일_인증_토큰_요청() throws Exception {
        // given
        Member member = createDefaultMember();
        SendEmailTokenRequestDto request = SendEmailTokenRequestDto.builder()
                .email(member.getEmail())
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/send-email-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 유효한_메일_토큰_확인() throws Exception {
        // given
        EmailToken emailToken = EmailToken.builder()
                .email("a@a.com")
                .tokenValue("abcdef")
                .build();
        emailTokenRepository.save(emailToken);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/auth/check-email-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("email", emailToken.getEmail())
                        .queryParam("tokenValue", emailToken.getTokenValue()))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        EmailToken foundEmailToken = emailTokenRepository.findById(emailToken.getEmail()).get();
        assertThat(foundEmailToken.authenticated()).isTrue();
    }

    @Test
    void 유효하지_않은_메일_토큰_확인() throws Exception {
        // given
        EmailToken emailToken = EmailToken.builder()
                .email("a@a.com")
                .tokenValue("abcdef")
                .build();
        emailTokenRepository.save(emailToken);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/auth/check-email-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("email", emailToken.getEmail())
                        .queryParam("tokenValue", "123456"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        EmailToken foundEmailToken = emailTokenRepository.findById(emailToken.getEmail()).get();
        assertThat(foundEmailToken.authenticated()).isFalse();
    }

    @Test
    void 정상_이메일로_로그인() throws Exception {
        // given
        Member member = createDefaultMember();

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname(member.getEmail())
                .password("passwordA")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        TokenResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8),
                TokenResponseDto.class);
        assertThat(responseDto.getAccessToken()).isNotBlank();
        assertThat(responseDto.getRefreshToken()).isNotBlank();
        assertThat(refreshTokenRepository.findById(member.getEmail())).isNotEmpty();
    }

    @Test
    void 존재하지_않는_이메일로_로그인() throws Exception {
        // given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("a@a.com")
                .password("passwordA")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        ErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8),
                ErrorResponseDto.class);

        assertThat(responseDto.getMessage()).isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 정상_닉네임으로_로그인() throws Exception {
        // given
        Member member = createDefaultMember();

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname(member.getNickname())
                .password("passwordA")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        TokenResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8),
                TokenResponseDto.class);
        assertThat(responseDto.getAccessToken()).isNotBlank();
        assertThat(responseDto.getRefreshToken()).isNotBlank();
        assertThat(refreshTokenRepository.findById(member.getEmail())).isNotEmpty();
    }

    @Test
    void 존재하지_않는_닉네임으로_로그인() throws Exception {
        // given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("nameA")
                .password("passwordA")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        ErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8),
                ErrorResponseDto.class);

        assertThat(responseDto.getMessage()).isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 로그아웃() throws Exception {
        // given
        Member member = createDefaultMember();
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("nameA")
                .password("passwordA")
                .build();
        MockHttpServletResponse loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse();

        String accessToken = objectMapper.readValue(loginResponse.getContentAsString(UTF_8),
                TokenResponseDto.class).getAccessToken();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(refreshTokenRepository.findById(member.getEmail())).isEmpty();
    }

    @Test
    void sns_login시_신규_회원인_경우_회원_가입이_진행된다() throws Exception {
        // given
        when(socialClients.requestAccountResponse(any(), any()))
                .thenReturn(SocialAccountResponse.builder()
                        .email("a@a.com")
                        .nickname("gmelon")
                        .build());

        SnsLoginRequestDto requestDto = SnsLoginRequestDto.builder()
                .token("token")
                .memberType(MemberType.KAKAO)
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/sns-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn().getResponse();

        // then
        assertThat(memberRepository.findByEmail("a@a.com")).isPresent();
    }

    @Test
    void 정상_Refresh_Token으로_토큰_갱신() throws Exception {
        // given
        Member member = createDefaultMember();
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("nameA")
                .password("passwordA")
                .build();
        MockHttpServletResponse loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse();

        String refreshToken = objectMapper.readValue(loginResponse.getContentAsString(UTF_8),
                TokenResponseDto.class).getRefreshToken();
        TokenRefreshRequestDto tokenRefreshRequestDto = TokenRefreshRequestDto.builder()
                .refreshToken(refreshToken)
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/token-refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRefreshRequestDto)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        TokenResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8),
                TokenResponseDto.class);
        assertThat(responseDto.getAccessToken()).isNotBlank();
        assertThat(responseDto.getRefreshToken()).isNotBlank();
        assertThat(refreshTokenRepository.findById(member.getEmail())).isPresent();
    }

    @Test
    void 비정상_Refresh_Token으로_토큰_갱신() throws Exception {
        // given
        Member member = createDefaultMember();
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .emailOrNickname("nameA")
                .password("passwordA")
                .build();
        MockHttpServletResponse loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse();

        String refreshToken = objectMapper.readValue(loginResponse.getContentAsString(UTF_8),
                TokenResponseDto.class).getRefreshToken();
        TokenRefreshRequestDto tokenRefreshRequestDto = TokenRefreshRequestDto.builder()
                .refreshToken(refreshToken + "unvalid")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/token-refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRefreshRequestDto)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }


    // TODO refresh token 갱신 요청 시 동시성 문제 해결
//    @Test
//    void 동일한_Refresh_Token으로_2회_이상_토큰_갱신_시도() throws Exception {
//        // given
//        SignupRequestDto signupRequest = SignupRequestDto.builder()
//                .email("a@a.com")
//                .password("passwordA")
//                .nickname("nameA")
//                .build();
//        authService.signup(signupRequest);
//        LoginRequestDto loginRequest = LoginRequestDto.builder()
//                .emailOrNickname("nameA")
//                .password("passwordA")
//                .build();
//        MockHttpServletResponse loginResponse = mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andReturn().getResponse();
//
//        String refreshToken = objectMapper.readValue(loginResponse.getContentAsString(UTF_8),
//                TokenResponseDto.class).getRefreshToken();
//        TokenRefreshRequestDto tokenRefreshRequestDto = TokenRefreshRequestDto.builder()
//                .refreshToken(refreshToken)
//                .build();
//
//        // when
//        int executeCount = 2;
//        CountDownLatch countDownLatch = new CountDownLatch(executeCount);
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        AtomicReference<MockHttpServletResponse> response = new AtomicReference<>();
//        for (int i = 0; i < executeCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    response.set(mockMvc.perform(post("/api/auth/token-refresh")
//                                    .contentType(MediaType.APPLICATION_JSON)
//                                    .content(objectMapper.writeValueAsString(tokenRefreshRequestDto)))
//                            .andReturn().getResponse());
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    countDownLatch.countDown();
//                }
//            });
//        }
//
//        countDownLatch.await(10, TimeUnit.SECONDS);
//        executorService.shutdown();
//
//        // then
//        assertThat(response.get().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
//
//        assertThat(refreshTokenRepository.findById(signupRequest.getEmail())).isEmpty();
//    }
    @PlangoMockUser
    @Test
    void 회원이_스스로_회원_탈퇴() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule personalSchedule = Schedule.builder()
                .title("개인 일정")
                .date(LocalDate.now())
                .startTime(LocalTime.now())
                .endTime(LocalTime.now())
                .build();
        personalSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(member, personalSchedule),
                ScheduleMember.createParticipant(anotherMember, personalSchedule)
        ));
        scheduleRepository.save(personalSchedule);
        List<Diary> diaries = List.of(Diary.builder()
                        .member(member)
                        .schedule(personalSchedule)
                        .content("개인 일정 - owner 기록")
                        .build(),
                Diary.builder()
                        .member(anotherMember)
                        .schedule(personalSchedule)
                        .content("개인 일정 - 참가자 기록")
                        .build()
        );
        diaryRepository.saveAll(diaries);

        Schedule participatingSchedule = Schedule.builder()
                .title("참여 일정")
                .date(LocalDate.now())
                .startTime(LocalTime.now())
                .endTime(LocalTime.now())
                .build();
        participatingSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(anotherMember, participatingSchedule),
                ScheduleMember.createParticipant(member, participatingSchedule)
        ));
        scheduleRepository.save(participatingSchedule);

        Diary diary = Diary.builder()
                .member(member)
                .schedule(participatingSchedule)
                .content("참여 일정 기록")
                .build();
        diaryRepository.save(diary);

        Notification notification = Notification.builder()
                .title("알림 제목")
                .member(member)
                .build();
        notificationRepository.save(notification);

        PlaceSearchRecord placeSearchRecord = PlaceSearchRecord.builder()
                .keyword("검색어")
                .member(member)
                .lastSearchedDate(LocalDateTime.now())
                .build();
        placeSearchRecordRepository.save(placeSearchRecord);

        List<FirebaseCloudMessageToken> memberTokens = List.of(
                FirebaseCloudMessageToken.builder()
                        .tokenValue("123-abc")
                        .member(member)
                        .lastUpdatedDate(LocalDateTime.now())
                        .build(),
                FirebaseCloudMessageToken.builder()
                        .tokenValue("456-def")
                        .member(member)
                        .lastUpdatedDate(LocalDateTime.now())
                        .build()
        );
        firebaseCloudMessageTokenRepository.saveAll(memberTokens);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/auth/signout"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(scheduleQueryRepository.countByMemberId(member.getId())).isEqualTo(0);
        assertThat(scheduleRepository.findByIdWithMembers(participatingSchedule.getId()).get().getScheduleMembers())
                .hasSize(1);
        assertThat(diaryRepository.findByContent(diary.getContent())).isEmpty();
        assertThat(notificationRepository.findAllByMemberId(member.getId(), 0)).hasSize(0);
        assertThat(placeSearchRecordRepository.findAllByMemberId(member.getId(), 0)).hasSize(0);
        assertThat(firebaseCloudMessageTokenRepository.findAllByMember(member)).hasSize(0);
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }

    @PlangoMockUser(email = "admin@admin.com", nickname = "admin", role = MemberRole.ROLE_ADMIN)
    @Test
    void 관리자를_통한_회원_탈퇴() throws Exception {
        // given
        Member member = Member.builder()
                .email("a@a.com")
                .nickname("nameA")
                .password("passwordA")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .build();
        Member savedMember = memberRepository.save(member);

        Schedule schedule = Schedule.builder()
                .title("일정 제목")
                .date(LocalDate.now())
                .startTime(LocalTime.now())
                .endTime(LocalTime.now())
                .build();
        schedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(schedule);

        Diary diary = Diary.builder()
                .member(savedMember)
                .schedule(schedule)
                .content("기록 본문")
                .build();
        diaryRepository.save(diary);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/auth/signout/" + member.getId()))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(scheduleQueryRepository.countByMemberId(member.getId())).isEqualTo(0);
        assertThat(diaryRepository.findByContent(diary.getContent())).isEmpty();
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }

    @PlangoMockUser
    @Test
    void 일반_회원이_다른_회원의_회원_탈퇴_시도() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/auth/signout/" + 2))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 비밀번호_초기화_시_메일이_발송된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        String oldPassword = member.getPassword();
        PasswordResetRequestDto request = PasswordResetRequestDto.builder()
                .email(member.getEmail())
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        member = memberRepository.findAll().get(0);
        assertThat(member.getPassword()).isNotEqualTo(oldPassword);

        verify(emailSender).send(any());
    }

    @PlangoMockUser(type = MemberType.KAKAO)
    @Test
    void 소셜_계정의_비밀번호_초기화_요청_시_예외가_발생한다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        PasswordResetRequestDto request = PasswordResetRequestDto.builder()
                .email(member.getEmail())
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private Member createDefaultMember() {
        Member member = Member.builder()
                .email("a@a.com")
                .password(passwordEncoder.encode("passwordA"))
                .nickname("nameA")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .termsAccepted(true)
                .build();
        return memberRepository.save(member);
    }

    private Member createAnotherMember() {
        Member member = Member.builder()
                .email("b@b.com")
                .password(passwordEncoder.encode("passwordB"))
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .build();
        return memberRepository.save(member);
    }

}
