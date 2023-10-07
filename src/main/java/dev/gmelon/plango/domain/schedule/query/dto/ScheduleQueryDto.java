package dev.gmelon.plango.domain.schedule.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ScheduleQueryDto {

    private Long scheduleId;

    private String title;

    private String content;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    @Setter
    private List<ScheduleMemberQueryDto> scheduleMembers;

    @Setter
    private List<SchedulePlaceQueryDto> schedulePlaces;

    private Boolean isDone;

    private Boolean hasDiary;

    @QueryProjection
    @Builder
    public ScheduleQueryDto(Long scheduleId, String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime,
                            Boolean isDone, Boolean hasDiary) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDone = isDone;
        this.hasDiary = hasDiary;
    }

    @Getter
    public static class ScheduleMemberQueryDto {

        private Long memberId;

        private String nickname;

        private String profileImageUrl;

        private Boolean isOwner;

        private Boolean isAccepted;

        private Boolean isCurrentMember;

        @QueryProjection
        @Builder
        public ScheduleMemberQueryDto(Long memberId, String nickname, String profileImageUrl, Boolean isOwner, Boolean isAccepted, Boolean isCurrentMember) {
            this.memberId = memberId;
            this.nickname = nickname;
            this.profileImageUrl = profileImageUrl;
            this.isOwner = isOwner;
            this.isAccepted = isAccepted;
            this.isCurrentMember = isCurrentMember;
        }
    }

    @Getter
    public static class SchedulePlaceQueryDto {

        private Long placeId;

        private String placeName;

        private String roadAddress;

        private Double latitude;

        private Double longitude;

        private String memo;

        private String category;

        private Boolean isConfirmed;

        @Setter
        private List<Long> likedMemberIds;

        @QueryProjection
        @Builder

        public SchedulePlaceQueryDto(Long placeId, String placeName, String roadAddress, Double latitude, Double longitude,
                                     String memo, String category, Boolean isConfirmed) {
            this.placeId = placeId;
            this.placeName = placeName;
            this.roadAddress = roadAddress;
            this.latitude = latitude;
            this.longitude = longitude;
            this.memo = memo;
            this.category = category;
            this.isConfirmed = isConfirmed;
        }
    }

}
