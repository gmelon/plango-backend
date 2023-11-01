package dev.gmelon.plango.web.auth;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.auth.dto.LoginRequestDto;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageToken;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.domain.place.PlaceSearchRecord;
import dev.gmelon.plango.domain.place.PlaceSearchRecordRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.query.ScheduleQueryRepository;
import dev.gmelon.plango.exception.dto.ErrorResponseDto;
import dev.gmelon.plango.exception.dto.InputInvalidErrorResponseDto;
import dev.gmelon.plango.service.auth.AuthService;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Test
    void 정상_값으로_회원가입() throws Exception {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("a@a.com")
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
    }

    @Test
    void 이미_존재하는_이메일로_회원가입() throws Exception {
        // given
        SignupRequestDto firstRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        authService.signup(firstRequest);

        SignupRequestDto secondRequest = SignupRequestDto.builder()
                .email("a@a.com")
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

        InputInvalidErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), InputInvalidErrorResponseDto.class);

        assertThat(responseDto.getField()).isEqualTo("email");
        assertThat(responseDto.getMessage()).isEqualTo("이미 존재하는 이메일입니다.");
    }

    @Test
    void 이미_존재하는_닉네임으로_회원가입() throws Exception {
        // given
        SignupRequestDto firstRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        authService.signup(firstRequest);

        SignupRequestDto secondRequest = SignupRequestDto.builder()
                .email("b@b.com")
                .password("passwordA")
                .nickname("nameA")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        InputInvalidErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), InputInvalidErrorResponseDto.class);

        assertThat(responseDto.getField()).isEqualTo("nickname");
        assertThat(responseDto.getMessage()).isEqualTo("이미 존재하는 닉네임입니다.");
    }

    @Test
    void 정상_이메일로_로그인() throws Exception {
        // given
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        authService.signup(signupRequest);

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
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Set-Cookie")).isNotBlank();
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

        ErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), ErrorResponseDto.class);

        assertThat(responseDto.getMessage()).isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 정상_닉네임으로_로그인() throws Exception {
        // given
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .build();
        authService.signup(signupRequest);

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
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Set-Cookie")).isNotBlank();
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

        ErrorResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), ErrorResponseDto.class);

        assertThat(responseDto.getMessage()).isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @PlangoMockUser
    @Test
    void 로그아웃() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/logout"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

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
