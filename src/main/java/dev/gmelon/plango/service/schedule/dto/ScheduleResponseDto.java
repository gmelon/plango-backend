package dev.gmelon.plango.service.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleQueryDto;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleQueryDto.ScheduleMemberQueryDto;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleQueryDto.SchedulePlaceQueryDto;
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

    private Long scheduleId;

    private String title;

    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private LocalTime endTime;

    private List<ScheduleMemberResponseDto> scheduleMembers = new ArrayList<>();

    private List<SchedulePlaceResponseDto> schedulePlaces = new ArrayList<>();

    private Boolean isDone;

    private Boolean hasDiary;

    public static ScheduleResponseDto from(ScheduleQueryDto queryDto) {
        return ScheduleResponseDto.builder()
                .scheduleId(queryDto.getScheduleId())
                .title(queryDto.getTitle())
                .content(queryDto.getContent())
                .date(queryDto.getDate())
                .startTime(queryDto.getStartTime())
                .endTime(queryDto.getEndTime())
                .scheduleMembers(createScheduleMemberResponseDtos(queryDto.getScheduleMembers()))
                .schedulePlaces(createSchedulePlaceResponseDtos(queryDto.getSchedulePlaces()))
                .isDone(queryDto.getIsDone())
                .hasDiary(queryDto.getHasDiary())
                .build();
    }

    private static List<ScheduleMemberResponseDto> createScheduleMemberResponseDtos(List<ScheduleMemberQueryDto> queryDtos) {
        return queryDtos.stream()
                .map(ScheduleMemberResponseDto::from)
                .collect(toList());
    }

    private static List<SchedulePlaceResponseDto> createSchedulePlaceResponseDtos(List<SchedulePlaceQueryDto> queryDtos) {
        return queryDtos.stream()
                .map(SchedulePlaceResponseDto::from)
                .collect(toList());
    }

    @Builder
    public ScheduleResponseDto(
            Long scheduleId, String title, String content, LocalDate date, LocalTime startTime,
            LocalTime endTime, List<ScheduleMemberResponseDto> scheduleMembers, List<SchedulePlaceResponseDto> schedulePlaces,
            Boolean isDone, Boolean hasDiary
    ) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scheduleMembers = scheduleMembers;
        this.schedulePlaces = schedulePlaces;
        this.isDone = isDone;
        this.hasDiary = hasDiary;
    }

    @NoArgsConstructor
    @Getter
    public static class ScheduleMemberResponseDto {

        private Long memberId;

        private String nickname;

        private String profileImageUrl;

        private Boolean isOwner;

        private Boolean isAccepted;

        private Boolean isCurrentMember;

        public static ScheduleMemberResponseDto from(ScheduleMemberQueryDto queryDto) {
            return ScheduleMemberResponseDto.builder()
                    .memberId(queryDto.getMemberId())
                    .nickname(queryDto.getNickname())
                    .profileImageUrl(queryDto.getProfileImageUrl())
                    .isOwner(queryDto.getIsOwner())
                    .isAccepted(queryDto.getIsAccepted())
                    .isCurrentMember(queryDto.getIsCurrentMember())
                    .build();
        }

        @Builder
        public ScheduleMemberResponseDto(Long memberId, String nickname, String profileImageUrl, Boolean isOwner,
                                         Boolean isAccepted, Boolean isCurrentMember) {
            this.memberId = memberId;
            this.nickname = nickname;
            this.profileImageUrl = profileImageUrl;
            this.isOwner = isOwner;
            this.isAccepted = isAccepted;
            this.isCurrentMember = isCurrentMember;
        }
    }

    @NoArgsConstructor
    @Getter
    public static class SchedulePlaceResponseDto {

        private Long placeId;

        private String placeName;

        private String roadAddress;

        private Double latitude;

        private Double longitude;

        private String memo;

        private String category;

        private Boolean isConfirmed;

        private List<Long> likedMemberIds;

        @Builder
        public SchedulePlaceResponseDto(Long placeId, String placeName, String roadAddress, Double latitude, Double longitude,
                                        String memo, String category, Boolean isConfirmed, List<Long> likedMemberIds) {
            this.placeId = placeId;
            this.placeName = placeName;
            this.roadAddress = roadAddress;
            this.latitude = latitude;
            this.longitude = longitude;
            this.memo = memo;
            this.category = category;
            this.isConfirmed = isConfirmed;
            this.likedMemberIds = likedMemberIds;
        }

        public static SchedulePlaceResponseDto from(SchedulePlaceQueryDto queryDto) {
            return SchedulePlaceResponseDto.builder()
                    .placeId(queryDto.getPlaceId())
                    .placeName(queryDto.getPlaceName())
                    .roadAddress(queryDto.getRoadAddress())
                    .latitude(queryDto.getLatitude())
                    .longitude(queryDto.getLongitude())
                    .memo(queryDto.getMemo())
                    .category(queryDto.getCategory())
                    .isConfirmed(queryDto.getIsConfirmed())
                    .likedMemberIds(queryDto.getLikedMemberIds())
                    .build();
        }

    }

}
