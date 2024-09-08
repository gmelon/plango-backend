package dev.gmelon.plango.domain.schedule.repository.query;

import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleCountQueryDto;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleListQueryDto;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleQueryDto;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleTitleQueryDto;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleQueryRepositoryCustom {

    ScheduleQueryDto findOneById(Long scheduleId, Long memberId);

    List<ScheduleCountQueryDto> countBetweenDatesByMemberId(Long memberId, LocalDate startDate, LocalDate endDate);

    List<ScheduleTitleQueryDto> titlesBetweenDatesByMemberId(Long memberId, LocalDate startDate, LocalDate endDate);

    List<ScheduleListQueryDto> findAllByMemberIdAndDate(Long memberId, LocalDate date);

    int countByMemberId(Long memberId);
}
