package dev.gmelon.plango.service.member.dto;

import dev.gmelon.plango.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MemberSearchResponseDto {

    private Long id;
    private String nickname;
    private String profileImageUrl;

    public static MemberSearchResponseDto from(Member member) {
        return MemberSearchResponseDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    @Builder
    public MemberSearchResponseDto(Long id, String nickname, String profileImageUrl) {
        this.id = id;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
