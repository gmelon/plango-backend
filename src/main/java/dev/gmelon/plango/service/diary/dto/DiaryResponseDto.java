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
public class DiaryResponseDto {

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private ScheduleOfDiaryResponseDto schedule;

    public static DiaryResponseDto from(Schedule schedule) {
        ScheduleOfDiaryResponseDto scheduleResponse = ScheduleOfDiaryResponseDto.from(schedule);
        Diary diary = schedule.getDiary();

        return DiaryResponseDto.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .schedule(scheduleResponse)
                .build();
    }

    @Builder
    public DiaryResponseDto(Long id, String title, String content, String imageUrl, ScheduleOfDiaryResponseDto schedule) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.schedule = schedule;
    }

    @NoArgsConstructor
    @Getter
    public static class ScheduleOfDiaryResponseDto {

        private Long id;

        private String title;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime startTime;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime endTime;

        private String location;

        public static ScheduleOfDiaryResponseDto from(Schedule schedule) {
            return ScheduleOfDiaryResponseDto.builder()
                    .id(schedule.getId())
                    .title(schedule.getTitle())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .location(schedule.getLocation())
                    .build();
        }

        @Builder
        public ScheduleOfDiaryResponseDto(Long id, String title, LocalDateTime startTime, LocalDateTime endTime, String location) {
            this.id = id;
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
        }
    }

}
