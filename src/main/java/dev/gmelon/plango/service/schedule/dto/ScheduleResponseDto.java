package dev.gmelon.plango.service.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class ScheduleResponseDto {

    private Long id;

    private String title;

    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endTime;

    private String location;

    private Boolean isDone;

    private Boolean hasDiary;

    private DiaryOfScheduleResponseDto diary;

    public static ScheduleResponseDto of(Schedule schedule) {
        ScheduleResponseDtoBuilder builder = ScheduleResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .location(schedule.getLocation())
                .isDone(schedule.isDone())
                .hasDiary(false);

        if (schedule.getDiary() != null) {
            builder = builder.hasDiary(true)
                    .diary(DiaryOfScheduleResponseDto.of(schedule.getDiary()));
        }

        return builder.build();
    }

    @Builder
    public ScheduleResponseDto(Long id, String title, String content, LocalDateTime startTime, LocalDateTime endTime,
                               String location, Boolean isDone, Boolean hasDiary, DiaryOfScheduleResponseDto diary) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.isDone = isDone;
        this.hasDiary = hasDiary;
        this.diary = diary;
    }

    @NoArgsConstructor
    @Getter
    public static class DiaryOfScheduleResponseDto {
        private Long id;

        public static DiaryOfScheduleResponseDto of(Diary diary) {
            return DiaryOfScheduleResponseDto.builder()
                    .id(diary.getId())
                    .build();
        }

        @Builder
        public DiaryOfScheduleResponseDto(Long id) {
            this.id = id;
        }
    }
}
