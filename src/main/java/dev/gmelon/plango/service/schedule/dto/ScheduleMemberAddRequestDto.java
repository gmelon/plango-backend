package dev.gmelon.plango.service.schedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class ScheduleMemberAddRequestDto {

    @NotNull
    private Long memberId;

    @Builder
    public ScheduleMemberAddRequestDto(Long memberId) {
        this.memberId = memberId;
    }
}
