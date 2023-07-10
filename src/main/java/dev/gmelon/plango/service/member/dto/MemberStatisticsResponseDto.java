package dev.gmelon.plango.service.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MemberStatisticsResponseDto {

    private int scheduleCount;
    private int doneScheduleCount;
    private int diaryCount;

    @Builder
    public MemberStatisticsResponseDto(int scheduleCount, int doneScheduleCount, int diaryCount) {
        this.scheduleCount = scheduleCount;
        this.doneScheduleCount = doneScheduleCount;
        this.diaryCount = diaryCount;
    }
}
