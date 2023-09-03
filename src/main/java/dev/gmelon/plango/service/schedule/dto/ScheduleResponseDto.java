package dev.gmelon.plango.service.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

    private Boolean isOwner;

    private Boolean isAccepted;

    private List<ScheduleMemberResponseDto> scheduleMembers = new ArrayList<>();

    private Double latitude;

    private Double longitude;

    private String roadAddress;

    private String placeName;

    private Boolean isDone;

    private Boolean hasDiary;

    public static ScheduleResponseDto from(Schedule schedule, Member member, boolean hasDiary) {
        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .date(schedule.getDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .isOwner(schedule.isOwner(member.getId()))
                .isAccepted(schedule.isAccepted(member.getId()))
                .scheduleMembers(mapToResponseDto(schedule.getScheduleMembers()))
                .latitude(schedule.getLatitude())
                .longitude(schedule.getLongitude())
                .roadAddress(schedule.getRoadAddress())
                .placeName(schedule.getPlaceName())
                .isDone(schedule.isDone())
                .hasDiary(hasDiary)
                .build();
    }

    private static List<ScheduleMemberResponseDto> mapToResponseDto(List<ScheduleMember> scheduleMembers) {
        return scheduleMembers.stream()
                .map(ScheduleMemberResponseDto::from)
                .collect(toList());
    }

    @Builder
    public ScheduleResponseDto(
            Long id, String title, String content, LocalDate date, LocalTime startTime,
            LocalTime endTime, Boolean isOwner, Boolean isAccepted, List<ScheduleMemberResponseDto> scheduleMembers,
            Double latitude, Double longitude, String roadAddress, String placeName,
            Boolean isDone, Boolean hasDiary
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isOwner = isOwner;
        this.isAccepted = isAccepted;
        this.scheduleMembers = scheduleMembers;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
        this.isDone = isDone;
        this.hasDiary = hasDiary;
    }

    @NoArgsConstructor
    @Getter
    public static class ScheduleMemberResponseDto {

        private Long id;
        private String nickname;
        private Boolean isAccepted;

        public static ScheduleMemberResponseDto from(ScheduleMember scheduleMember) {
            Member member = scheduleMember.getMember();

            return ScheduleMemberResponseDto.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .isAccepted(scheduleMember.isAccepted())
                    .build();
        }

        @Builder
        public ScheduleMemberResponseDto(Long id, String nickname, Boolean isAccepted) {
            this.id = id;
            this.nickname = nickname;
            this.isAccepted = isAccepted;
        }
    }

}
