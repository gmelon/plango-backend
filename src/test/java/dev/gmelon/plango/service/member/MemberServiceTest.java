package dev.gmelon.plango.service.member;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.service.member.dto.MemberEditNicknameRequestDto;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberStatisticsResponseDto;
import dev.gmelon.plango.service.member.dto.PasswordChangeRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class MemberServiceTest {

    private Member memberA;

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        memberA = Member.builder()
                .email("a@a.com")
                .password(passwordEncoder.encode("passwordA"))
                .nickname("nameA")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberA);
    }

    @Test
    void 나의_프로필_조회() {
        // when
        MemberProfileResponseDto response = memberService.getMyProfile(memberA.getId());

        // then
        assertThat(response.getId()).isEqualTo(memberA.getId());
        assertThat(response.getNickname()).isEqualTo(memberA.getNickname());
        assertThat(response.getNickname()).isEqualTo(memberA.getNickname());
    }

    @Test
    void 나의_통계정보_조회() {
        // when
        MemberStatisticsResponseDto response = memberService.getMyStatistics(memberA.getId());

        // then
        assertThat(response.getScheduleCount()).isGreaterThanOrEqualTo(0);
        assertThat(response.getDoneScheduleCount())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(response.getScheduleCount());
        assertThat(response.getDiaryCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void 비밀번호_변경() {
        // given
        PasswordChangeRequestDto request = PasswordChangeRequestDto.builder()
                .previousPassword("passwordA")
                .newPassword("passwordB")
                .build();

        // when
        memberService.changePassword(memberA.getId(), request);

        // then
        Member foundMemberA = memberRepository.findById(memberA.getId()).get();
        assertThat(passwordEncoder.matches(request.getNewPassword(), foundMemberA.getPassword())).isTrue();
    }

    @Test
    void 잘못된_이전_비밀번호로_비밀번호_변경() {
        // given
        PasswordChangeRequestDto request = PasswordChangeRequestDto.builder()
                .previousPassword("passwordC")
                .newPassword("passwordB")
                .build();

        // when, then
        assertThatThrownBy(() -> memberService.changePassword(memberA.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이전 비밀번호가 일치하지 않습니다.");

        Member foundMemberA = memberRepository.findById(memberA.getId()).get();
        assertThat(passwordEncoder.matches("passwordA", foundMemberA.getPassword())).isTrue();
    }

    @Test
    void 이름_변경() {
        // given
        MemberEditNicknameRequestDto request = MemberEditNicknameRequestDto.builder()
                .nickname("nameB")
                .build();

        // when
        memberService.editNickname(memberA.getId(), request);

        //then
        Member foundMemberA = memberRepository.findById(memberA.getId()).get();
        assertThat(foundMemberA.getNickname()).isEqualTo(request.getNickname());
    }
}
