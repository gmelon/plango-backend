package dev.gmelon.plango.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void 회원_비밀번호를_변경한다() {
        // given
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        Member member = Member.builder()
                .password(oldPassword)
                .build();

        // when
        member.changePassword(newPassword);

        // then
        assertThat(member.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void 회원_role을_변경한다() {
        // given
        MemberRole oldRole = MemberRole.ROLE_USER;
        MemberRole newRole = MemberRole.ROLE_ADMIN;

        Member member = Member.builder()
                .role(oldRole)
                .build();

        // when
        member.changeRole(newRole);

        // then
        assertThat(member.getRole()).isEqualTo(newRole.toString());
    }

    @Test
    void 회원_role의_문자열을_반환한다() {
        // given
        MemberRole role = MemberRole.ROLE_USER;
        Member member = Member.builder()
                .role(role)
                .build();

        // when, then
        assertThat(member.getRole()).isEqualTo(role.toString());
    }

    @Test
    void 회원_정보를_수정한다() {
        // given
        Member member = Member.builder()
                .nickname("상혁")
                .bio("나는 상혁")
                .profileImageUrl("sh.com")
                .build();

        MemberEditor editor = MemberEditor.builder()
                .nickname("gmelon")
                .bio("나는 gmelon")
                .profileImageUrl("gmelon.com")
                .build();

        // when
        member.edit(editor);

        // then
        assertThat(member.getNickname()).isEqualTo(editor.getNickname());
        assertThat(member.getBio()).isEqualTo(editor.getBio());
        assertThat(member.getProfileImageUrl()).isEqualTo(editor.getProfileImageUrl());
    }

}
