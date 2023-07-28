package dev.gmelon.plango.service.auth;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.member.DuplicateEmailException;
import dev.gmelon.plango.exception.member.DuplicateNicknameException;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private DiaryRepository diaryRepository;


    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void 정상_값으로_회원가입() {
        // given
        String unencodedPassword = "passwordA";

        SignupRequestDto request = SignupRequestDto.builder()
                .email("a@a.com")
                .password(unencodedPassword)
                .nickname("nameA")
                .build();

        // when
        authService.signup(request);

        // then
        Member member = assertDoesNotThrow(() -> memberRepository.findByEmail(request.getEmail()).get());
        assertThat(passwordEncoder.matches(unencodedPassword, member.getPassword())).isTrue();
        assertThat(member.getNickname()).isEqualTo(request.getNickname());
    }

    @Test
    void 이미_존재하는_이메일로_회원가입() {
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

        // when, then
        assertThatThrownBy(() -> authService.signup(secondRequest))
                .isInstanceOf(DuplicateEmailException.class);
    }


    @Test
    void 이미_존재하는_닉네임으로_회원가입() {
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

        // when, then
        assertThatThrownBy(() -> authService.signup(secondRequest))
                .isInstanceOf(DuplicateNicknameException.class);
    }

    @Test
    void 회원_탈퇴() {
        // given
        Member member = Member.builder()
                .email("a@a.com")
                .nickname("nameA")
                .password(passwordEncoder.encode("passwordA"))
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(member);

        Diary diary = Diary.builder()
                .title("기록 제목")
                .build();
        Schedule schedule = Schedule.builder()
                .title("계획 제목")
                .date(LocalDate.now())
                .startTime(LocalTime.now())
                .endTime(LocalTime.now())
                .diary(diary)
                .member(member)
                .build();
        scheduleRepository.save(schedule);

        // when
        authService.signout(member.getId());

        // then
        assertThat(scheduleRepository.findAllByMemberId(member.getId())).hasSize(0);
        assertThat(diaryRepository.findByTitle(diary.getTitle())).isEmpty();
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }

}
