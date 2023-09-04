package dev.gmelon.plango.service.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class MemberSearchRequestDto {

    @NotBlank
    private String nickname;

    @Builder
    public MemberSearchRequestDto(String nickname) {
        this.nickname = nickname;
    }
}
