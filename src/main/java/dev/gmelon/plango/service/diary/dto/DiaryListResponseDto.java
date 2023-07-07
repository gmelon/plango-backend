package dev.gmelon.plango.service.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class DiaryListResponseDto {

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private ScheduleOfDiaryListResponseDto schedule;

    public static DiaryListResponseDto from(Schedule schedule) {
        ScheduleOfDiaryListResponseDto scheduleResponse = ScheduleOfDiaryListResponseDto.from(schedule);
        Diary diary = schedule.getDiary();

        return DiaryListResponseDto.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .schedule(scheduleResponse)
                .build();
    }

    @Builder
    public DiaryListResponseDto(Long id, String title, String content, String imageUrl, ScheduleOfDiaryListResponseDto schedule) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.schedule = schedule;
    }

    @NoArgsConstructor
    @Getter
    public static class ScheduleOfDiaryListResponseDto {

        private String title;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime startTime;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime endTime;

        private String location;

        public static ScheduleOfDiaryListResponseDto from(Schedule schedule) {
            return ScheduleOfDiaryListResponseDto.builder()
                    .title(schedule.getTitle())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .location(schedule.getLocation())
                    .build();
        }

        @Builder
        public ScheduleOfDiaryListResponseDto(String title, LocalDateTime startTime, LocalDateTime endTime, String location) {
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
        }
    }

}
