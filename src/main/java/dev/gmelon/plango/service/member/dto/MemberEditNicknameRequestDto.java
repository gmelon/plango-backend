package dev.gmelon.plango.service.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class MemberEditNicknameRequestDto {

    @NotBlank
    private String nickname;

    @Builder
    public MemberEditNicknameRequestDto(String nickname) {
        this.nickname = nickname;
    }
}
