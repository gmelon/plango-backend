package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class DiaryListResponseDto {

    private Long id;
    private String content;
    private String imageUrl;
    private ScheduleOfDiaryListResponseDto schedule;

    public static DiaryListResponseDto from(Schedule schedule) {
        ScheduleOfDiaryListResponseDto scheduleResponse = ScheduleOfDiaryListResponseDto.from(schedule);
        Diary diary = schedule.getDiary();

        return DiaryListResponseDto.builder()
                .id(diary.getId())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .schedule(scheduleResponse)
                .build();
    }

    @Builder
    public DiaryListResponseDto(Long id, String content, String imageUrl, ScheduleOfDiaryListResponseDto schedule) {
        this.id = id;
        this.content = content;
        this.imageUrl = imageUrl;
        this.schedule = schedule;
    }

    @NoArgsConstructor
    @Getter
    public static class ScheduleOfDiaryListResponseDto {

        private String title;

        public static ScheduleOfDiaryListResponseDto from(Schedule schedule) {
            return ScheduleOfDiaryListResponseDto.builder()
                    .title(schedule.getTitle())
                    .build();
        }

        @Builder
        public ScheduleOfDiaryListResponseDto(String title) {
            this.title = title;
        }
    }

}
