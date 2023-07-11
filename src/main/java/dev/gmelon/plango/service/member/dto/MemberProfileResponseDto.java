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
    private String name;

    public static MemberProfileResponseDto from(Member member) {
        return MemberProfileResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    @Builder
    public MemberProfileResponseDto(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
}
