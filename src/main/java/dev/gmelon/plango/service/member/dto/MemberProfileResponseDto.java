package dev.gmelon.plango.service.member.dto;

import dev.gmelon.plango.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MemberProfileResponseDto {

    private Long id;
    private String email;
    private String nickname;

    public static MemberProfileResponseDto from(Member member) {
        return MemberProfileResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
    }

    @Builder
    public MemberProfileResponseDto(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }
}
