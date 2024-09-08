package dev.gmelon.plango.domain.auth.dto;

import dev.gmelon.plango.domain.member.entity.MemberType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SnsRevokeRequestDto {
    @NotNull
    private MemberType memberType;

    @NotBlank
    private String token;

    private Long snsTargetId;

    @Builder
    public SnsRevokeRequestDto(MemberType memberType, String token, Long snsTargetId) {
        this.memberType = memberType;
        this.token = token;
        this.snsTargetId = snsTargetId;
    }
}
