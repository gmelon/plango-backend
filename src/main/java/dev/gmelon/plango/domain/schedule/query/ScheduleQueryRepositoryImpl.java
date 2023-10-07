package dev.gmelon.plango.domain.schedule.query;

import com.querydsl.core.annotations.QueryProjection;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.gmelon.plango.domain.schedule.query.dto.*;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleQueryDto.ScheduleMemberQueryDto;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleQueryDto.SchedulePlaceQueryDto;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static dev.gmelon.plango.domain.diary.QDiary.diary;
import static dev.gmelon.plango.domain.member.QMember.member;
import static dev.gmelon.plango.domain.schedule.QSchedule.schedule;
import static dev.gmelon.plango.domain.schedule.QScheduleMember.scheduleMember;
import static dev.gmelon.plango.domain.schedule.place.QSchedulePlace.schedulePlace;
import static dev.gmelon.plango.domain.schedule.place.QSchedulePlaceLike.schedulePlaceLike;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
public class ScheduleQueryRepositoryImpl implements ScheduleQueryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public ScheduleQueryDto findOneById(Long scheduleId, Long memberId) {
        ScheduleQueryDto scheduleDto = fetchSchedule(scheduleId, memberId);
        scheduleDto.setScheduleMembers(fetchScheduleMembers(scheduleId, memberId));
        scheduleDto.setSchedulePlaces(fetchSchedulePlaces(scheduleId));
        return scheduleDto;
    }

    private ScheduleQueryDto fetchSchedule(Long scheduleId, Long memberId) {
        return jpaQueryFactory
                .select(new QScheduleQueryDto(
                        schedule.id,
                        schedule.title,
                        schedule.content,
                        schedule.date,
                        schedule.startTime,
                        schedule.endTime,
                        schedule.done,
                        hasDiary(scheduleId, memberId)
                ))
                .from(schedule)
                .where(schedule.id.eq(scheduleId))
                .fetchOne();
    }

    private BooleanExpression hasDiary(Long scheduleId, Long memberId) {
        return JPAExpressions.select(diary.id)
                .from(diary)
                .where(diary.schedule.id.eq(scheduleId)
                        .and(diary.member.id.eq(memberId)))
                .exists();
    }

    private List<ScheduleMemberQueryDto> fetchScheduleMembers(Long scheduleId, Long memberId) {
        return jpaQueryFactory
                .select(new QScheduleQueryDto_ScheduleMemberQueryDto(
                        scheduleMember.member.id,
                        scheduleMember.member.nickname,
                        scheduleMember.member.profileImageUrl,
                        scheduleMember.owner,
                        scheduleMember.accepted,
                        isCurrentMember(memberId)
                ))
                .from(scheduleMember)
                .join(scheduleMember.member, member)
                .where(scheduleMember.schedule.id.eq(scheduleId))
                .fetch();
    }

    private BooleanExpression isCurrentMember(Long memberId) {
        return scheduleMember.member.id.eq(memberId);
    }

    private List<SchedulePlaceQueryDto> fetchSchedulePlaces(Long scheduleId) {
        List<SchedulePlaceQueryDto> schedulePlaceDtos = jpaQueryFactory
                .select(new QScheduleQueryDto_SchedulePlaceQueryDto(
                        schedulePlace.id,
                        schedulePlace.placeName,
                        schedulePlace.roadAddress,
                        schedulePlace.latitude,
                        schedulePlace.longitude,
                        schedulePlace.memo,
                        schedulePlace.category,
                        schedulePlace.confirmed
                ))
                .from(schedulePlace)
                .where(schedulePlace.schedule.id.eq(scheduleId))
                .fetch();

        List<SchedulePlaceLikeQueryDto> schedulePlaceLikeDtos = fetchSchedulePlaceLikes(schedulePlaceDtos);
        setLikesToPlaces(schedulePlaceDtos, schedulePlaceLikeDtos);

        return schedulePlaceDtos;
    }

    private List<SchedulePlaceLikeQueryDto> fetchSchedulePlaceLikes(List<SchedulePlaceQueryDto> schedulePlaceDtos) {
        List<Long> schedulePlaceIds = schedulePlaceDtos.stream()
                .map(SchedulePlaceQueryDto::getPlaceId)
                .collect(toList());

        return jpaQueryFactory
                .select(new QScheduleQueryRepositoryImpl_SchedulePlaceLikeQueryDto(
                        schedulePlaceLike.schedulePlace.id,
                        schedulePlaceLike.member.id
                ))
                .from(schedulePlaceLike)
                .where(schedulePlaceLike.schedulePlace.id.in(schedulePlaceIds))
                .fetch();
    }

    private void setLikesToPlaces(List<SchedulePlaceQueryDto> schedulePlaceDtos, List<SchedulePlaceLikeQueryDto> schedulePlaceLikeDtos) {
        Map<Long, List<Long>> likeMemberIdsMap = schedulePlaceLikeDtos.stream()
                .collect(groupingBy(SchedulePlaceLikeQueryDto::getPlaceId, mapping(SchedulePlaceLikeQueryDto::getMemberId, toList())));

        schedulePlaceDtos
                .forEach(schedulePlaceDto -> schedulePlaceDto.setLikedMemberIds(likeMemberIdsMap.getOrDefault(schedulePlaceDto.getPlaceId(), emptyList())));
    }

    @Getter
    static public class SchedulePlaceLikeQueryDto {

        private Long placeId;
        private Long memberId;

        @QueryProjection
        @Builder
        public SchedulePlaceLikeQueryDto(Long placeId, Long memberId) {
            this.placeId = placeId;
            this.memberId = memberId;
        }

    }

    @Override
    public List<ScheduleCountQueryDto> countOfDaysByMemberId(Long memberId, LocalDate startDate, LocalDate endDate) {
        NumberExpression<Integer> doneSchedule = new CaseBuilder().when(schedule.done.isTrue()).then(1).otherwise(0);

        return jpaQueryFactory
                .select(new QScheduleCountQueryDto(
                        schedule.date,
                        doneSchedule.sum(),
                        schedule.count().intValue()
                ))
                .from(schedule)
                .join(schedule.scheduleMembers, scheduleMember)
                .on(scheduleMember.member.id.eq(memberId))
                .where(schedule.date.between(startDate, endDate))
                .groupBy(schedule.date)
                .having(schedule.count().gt(0))
                .orderBy(schedule.date.asc())
                .fetch();
    }

    @Override
    public List<ScheduleListQueryDto> findAllByMemberIdAndDate(Long memberId, LocalDate date) {
        List<ScheduleListQueryDto> scheduleListDtos = jpaQueryFactory
                .select(new QScheduleListQueryDto(
                        schedule.id,
                        schedule.title,
                        schedule.content,
                        schedule.date,
                        schedule.startTime,
                        schedule.endTime,
                        schedule.scheduleMembers.size(),
                        scheduleMember.owner,
                        scheduleMember.accepted,
                        schedule.done))
                .from(schedule)
                .join(schedule.scheduleMembers, scheduleMember)
                .where(scheduleMember.member.id.eq(memberId)
                        .and(schedule.date.eq(date)))
                .orderBy(
                        schedule.startTime.asc().nullsFirst(),
                        schedule.endTime.asc().nullsFirst(),
                        schedule.createdTime.asc())
                .fetch();

        setConfirmedSchedulePlaces(scheduleListDtos);
        return scheduleListDtos;
    }

    private void setConfirmedSchedulePlaces(List<ScheduleListQueryDto> scheduleListDtos) {
        List<Long> scheduleIds = mapToIds(scheduleListDtos);

        Map<Long, String> confirmedSchedulePlacesJoiningMap = fetchConfirmedSchedulePlaces(scheduleIds).stream()
                .collect(groupingBy(ConfirmedSchedulePlaceQueryDto::getScheduleId, mapping(ConfirmedSchedulePlaceQueryDto::getPlaceName, joining(", "))));

        for (ScheduleListQueryDto scheduleListDto : scheduleListDtos) {
            scheduleListDto.setConfirmedPlaceNames(confirmedSchedulePlacesJoiningMap.get(scheduleListDto.getId()));
        }
    }

    private List<Long> mapToIds(List<ScheduleListQueryDto> scheduleListDtos) {
        return scheduleListDtos.stream()
                .map(ScheduleListQueryDto::getId)
                .collect(toList());
    }

    private List<ConfirmedSchedulePlaceQueryDto> fetchConfirmedSchedulePlaces(List<Long> scheduleIds) {
        return jpaQueryFactory
                .select(new QScheduleQueryRepositoryImpl_ConfirmedSchedulePlaceQueryDto(
                        schedulePlace.schedule.id,
                        schedulePlace.placeName
                ))
                .from(schedulePlace)
                .where(schedulePlace.confirmed.isTrue()
                        .and(schedulePlace.schedule.id.in(scheduleIds)))
                .fetch();
    }

    @Getter
    static public class ConfirmedSchedulePlaceQueryDto {

        private Long scheduleId;

        private String placeName;

        @QueryProjection
        @Builder
        public ConfirmedSchedulePlaceQueryDto(Long scheduleId, String placeName) {
            this.scheduleId = scheduleId;
            this.placeName = placeName;
        }
    }

    @Override
    public int countByMemberId(Long memberId) {
        return jpaQueryFactory
                .select(schedule.count())
                .from(schedule)
                .join(schedule.scheduleMembers, scheduleMember)
                .on(scheduleMember.member.id.eq(memberId))
                .fetchOne()
                .intValue();
    }
}
