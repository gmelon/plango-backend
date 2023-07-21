package dev.gmelon.plango.service.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

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

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate date;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
        private LocalTime startTime;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
        private LocalTime endTime;

        private String placeName;

        public static ScheduleOfDiaryResponseDto from(Schedule schedule) {
            return ScheduleOfDiaryResponseDto.builder()
                    .id(schedule.getId())
                    .title(schedule.getTitle())
                    .date(schedule.getDate())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .placeName(schedule.getPlaceName())
                    .build();
        }

        @Builder
        public ScheduleOfDiaryResponseDto(Long id, String title, LocalDate date, LocalTime startTime, LocalTime endTime, String placeName) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.placeName = placeName;
        }
    }

}
