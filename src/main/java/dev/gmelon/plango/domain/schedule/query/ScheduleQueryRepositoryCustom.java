package dev.gmelon.plango.domain.schedule.query;

import dev.gmelon.plango.domain.schedule.query.dto.ScheduleCountQueryDto;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleListQueryDto;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleQueryDto;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleQueryRepositoryCustom {

    ScheduleQueryDto findOneById(Long scheduleId, Long memberId);

    List<ScheduleCountQueryDto> countOfDaysByMemberId(Long memberId, LocalDate startDate, LocalDate endDate);

    List<ScheduleListQueryDto> findAllByMemberIdAndDate(Long memberId, LocalDate date);

    int countByMemberId(Long memberId);

}
