package dev.gmelon.plango.domain.diary.dto;

import dev.gmelon.plango.domain.diary.entity.Diary;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class DiaryDateListResponseDto {

    private Long id;
    private String content;
    private List<String> imageUrls = new ArrayList<>();
    private ScheduleOfDiaryDateListResponseDto schedule;

    public static DiaryDateListResponseDto from(Diary diary, Schedule schedule) {
        ScheduleOfDiaryDateListResponseDto scheduleResponse = ScheduleOfDiaryDateListResponseDto.from(schedule);

        return DiaryDateListResponseDto.builder()
                .id(diary.getId())
                .content(diary.getContent())
                .imageUrls(diary.getDiaryImageUrls())
                .schedule(scheduleResponse)
                .build();
    }

    @Builder
    public DiaryDateListResponseDto(Long id, String content, List<String> imageUrls, ScheduleOfDiaryDateListResponseDto schedule) {
        this.id = id;
        this.content = content;
        this.imageUrls = imageUrls;
        this.schedule = schedule;
    }

    @NoArgsConstructor
    @Getter
    public static class ScheduleOfDiaryDateListResponseDto {

        private String title;

        public static ScheduleOfDiaryDateListResponseDto from(Schedule schedule) {
            return ScheduleOfDiaryDateListResponseDto.builder()
                    .title(schedule.getTitle())
                    .build();
        }

        @Builder
        public ScheduleOfDiaryDateListResponseDto(String title) {
            this.title = title;
        }
    }

}
