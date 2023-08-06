package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class DiaryListResponseDto {

    private Long id;
    private String content;
    private List<String> imageUrls = new ArrayList<>();
    private ScheduleOfDiaryListResponseDto schedule;

    public static DiaryListResponseDto from(Schedule schedule) {
        ScheduleOfDiaryListResponseDto scheduleResponse = ScheduleOfDiaryListResponseDto.from(schedule);
        Diary diary = schedule.getDiary();

        return DiaryListResponseDto.builder()
                .id(diary.getId())
                .content(diary.getContent())
                .imageUrls(diary.getDiaryImageUrls())
                .schedule(scheduleResponse)
                .build();
    }

    @Builder
    public DiaryListResponseDto(Long id, String content, List<String> imageUrls, ScheduleOfDiaryListResponseDto schedule) {
        this.id = id;
        this.content = content;
        this.imageUrls = imageUrls;
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
