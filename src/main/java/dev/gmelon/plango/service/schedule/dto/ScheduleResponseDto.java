package dev.gmelon.plango.service.schedule.dto;

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
public class ScheduleResponseDto {

    private Long id;

    private String title;

    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private LocalTime endTime;

    private Double latitude;

    private Double longitude;

    private String roadAddress;

    private String placeName;

    private Boolean isDone;

    private Boolean hasDiary;

    private DiaryOfScheduleResponseDto diary;

    public static ScheduleResponseDto from(Schedule schedule) {
        ScheduleResponseDtoBuilder builder = ScheduleResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .date(schedule.getDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .latitude(schedule.getLatitude())
                .longitude(schedule.getLongitude())
                .roadAddress(schedule.getRoadAddress())
                .placeName(schedule.getPlaceName())
                .isDone(schedule.isDone())
                .hasDiary(false);

        builder = addDiaryWhenExists(schedule, builder);
        return builder.build();
    }

    private static ScheduleResponseDtoBuilder addDiaryWhenExists(Schedule schedule, ScheduleResponseDtoBuilder builder) {
        if (schedule.getDiary() != null) {
            builder = builder.hasDiary(true)
                    .diary(DiaryOfScheduleResponseDto.from(schedule.getDiary()));
        }
        return builder;
    }

    @Builder
    public ScheduleResponseDto(Long id, String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime, Double latitude, Double longitude, String roadAddress, String placeName, Boolean isDone, Boolean hasDiary, DiaryOfScheduleResponseDto diary) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
        this.isDone = isDone;
        this.hasDiary = hasDiary;
        this.diary = diary;
    }

    @NoArgsConstructor
    @Getter
    public static class DiaryOfScheduleResponseDto {
        private Long id;

        public static DiaryOfScheduleResponseDto from(Diary diary) {
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
