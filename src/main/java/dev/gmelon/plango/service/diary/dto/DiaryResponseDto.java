package dev.gmelon.plango.service.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class DiaryResponseDto {

    private Long id;
    private String content;
    private List<String> imageUrls = new ArrayList<>();
    private ScheduleOfDiaryResponseDto schedule;

    public static DiaryResponseDto from(Diary diary, Schedule schedule) {
        ScheduleOfDiaryResponseDto scheduleResponse = ScheduleOfDiaryResponseDto.from(schedule);

        return DiaryResponseDto.builder()
                .id(diary.getId())
                .content(diary.getContent())
                .imageUrls(diary.getDiaryImageUrls())
                .schedule(scheduleResponse)
                .build();
    }

    @Builder
    public DiaryResponseDto(Long id, String content, List<String> imageUrls, ScheduleOfDiaryResponseDto schedule) {
        this.id = id;
        this.content = content;
        this.imageUrls = imageUrls;
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

        public static ScheduleOfDiaryResponseDto from(Schedule schedule) {
            return ScheduleOfDiaryResponseDto.builder()
                    .id(schedule.getId())
                    .title(schedule.getTitle())
                    .date(schedule.getDate())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .build();
        }

        @Builder
        public ScheduleOfDiaryResponseDto(Long id, String title, LocalDate date, LocalTime startTime, LocalTime endTime) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

}
