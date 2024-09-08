package dev.gmelon.plango.domain.schedule.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
