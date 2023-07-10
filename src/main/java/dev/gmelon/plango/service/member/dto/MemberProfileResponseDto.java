package dev.gmelon.plango.service.member.dto;

import dev.gmelon.plango.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MemberProfileResponseDto {

    private String email;
    private String name;

    public static MemberProfileResponseDto from(Member member) {
        return MemberProfileResponseDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    @Builder
    public MemberProfileResponseDto(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
