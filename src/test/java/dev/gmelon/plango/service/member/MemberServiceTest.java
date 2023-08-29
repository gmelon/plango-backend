package dev.gmelon.plango.service.member;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.exception.member.PasswordMismatchException;
import dev.gmelon.plango.service.member.dto.MemberEditProfileRequestDto;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
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
                .profileImageUrl("https://plango-backend/imageA.jpg")
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
        assertThat(response.getProfileImageUrl()).isEqualTo(memberA.getProfileImageUrl());
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
                .isInstanceOf(PasswordMismatchException.class);

        Member foundMemberA = memberRepository.findById(memberA.getId()).get();
        assertThat(passwordEncoder.matches("passwordA", foundMemberA.getPassword())).isTrue();
    }

    @Test
    void 프로필_수정() {
        // given
        MemberEditProfileRequestDto request = MemberEditProfileRequestDto.builder()
                .nickname("nameB")
                .profileImageUrl("https://plango-backend/imageB.jpg")
                .build();

        // when
        memberService.editProfile(memberA.getId(), request);

        //then
        Member foundMemberA = memberRepository.findById(memberA.getId()).get();
        assertThat(foundMemberA.getNickname()).isEqualTo(request.getNickname());
        assertThat(foundMemberA.getProfileImageUrl()).isEqualTo(request.getProfileImageUrl());
    }
}
