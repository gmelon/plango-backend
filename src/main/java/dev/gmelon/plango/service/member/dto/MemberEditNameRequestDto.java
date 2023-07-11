package dev.gmelon.plango.service.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class MemberEditNameRequestDto {

    @NotBlank
    private String name;

    @Builder
    public MemberEditNameRequestDto(String name) {
        this.name = name;
    }
}
