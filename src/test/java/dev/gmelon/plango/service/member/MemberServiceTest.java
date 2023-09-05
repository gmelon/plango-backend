package dev.gmelon.plango.service.member;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.exception.member.PasswordMismatchException;
import dev.gmelon.plango.service.member.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

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
                .bio("소개 A")
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
        assertThat(response.getBio()).isEqualTo(memberA.getBio());
        assertThat(response.getProfileImageUrl()).isEqualTo(memberA.getProfileImageUrl());
    }

    @Test
    void 다른_회원의_프로필_조회() {
        // given
        Member memberB = Member.builder()
                .email("b@b.com")
                .password(passwordEncoder.encode("passwordB"))
                .nickname("nameB")
                .bio("소개 B")
                .profileImageUrl("https://plango-backend/imageB.jpg")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberB);

        // when
        MemberProfileResponseDto response = memberService.getProfile(memberA.getId(), memberB.getId());

        // then
        assertThat(response.getId()).isEqualTo(memberB.getId());
        assertThat(response.getNickname()).isEqualTo(memberB.getNickname());
        assertThat(response.getNickname()).isEqualTo(memberB.getNickname());
        assertThat(response.getBio()).isEqualTo(memberB.getBio());
        assertThat(response.getProfileImageUrl()).isEqualTo(memberB.getProfileImageUrl());
    }

    @Test
    void 닉네임으로_프로필_검색() {
        // given
        Member memberB = Member.builder()
                .email("b@b.com")
                .password(passwordEncoder.encode("passwordB"))
                .nickname("멤버 B 닉네임")
                .profileImageUrl("https://plango-backend/imageB.jpg")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberB);

        MemberSearchRequestDto request = MemberSearchRequestDto.builder()
                .nickname("버B닉")
                .build();

        // when
        List<MemberSearchResponseDto> responses = memberService.searchWithoutCurrentMember(memberA.getId(), request);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(memberB.getId());
    }

    @Test
    void 닉네임으로_프로필_검색시_자신은_검색되지_않음() {
        MemberSearchRequestDto request = MemberSearchRequestDto.builder()
                .nickname("nameA")
                .build();

        // when
        List<MemberSearchResponseDto> responses = memberService.searchWithoutCurrentMember(memberA.getId(), request);

        // then
        assertThat(responses).hasSize(0);
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
                .bio("소개 B")
                .profileImageUrl("https://plango-backend/imageB.jpg")
                .build();

        // when
        memberService.editProfile(memberA.getId(), request);

        //then
        Member foundMemberA = memberRepository.findById(memberA.getId()).get();
        assertThat(foundMemberA.getNickname()).isEqualTo(request.getNickname());
        assertThat(foundMemberA.getBio()).isEqualTo(request.getBio());
        assertThat(foundMemberA.getProfileImageUrl()).isEqualTo(request.getProfileImageUrl());
    }
}
