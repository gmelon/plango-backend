package dev.gmelon.plango.domain.member.dto;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.entity.MemberType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MemberProfileResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private String bio;
    private String profileImageUrl;
    private MemberType type;

    public static MemberProfileResponseDto from(Member member) {
        return MemberProfileResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .bio(member.getBio())
                .profileImageUrl(member.getProfileImageUrl())
                .type(member.getType())
                .build();
    }

    @Builder
    public MemberProfileResponseDto(Long id, String email, String nickname, String bio, String profileImageUrl,
                                    MemberType type) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.type = type;
    }
}
