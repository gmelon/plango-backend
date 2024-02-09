package dev.gmelon.plango.domain.member.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
