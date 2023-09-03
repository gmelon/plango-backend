package dev.gmelon.plango.domain.schedule.query;

import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleCountQueryDto;
import dev.gmelon.plango.domain.schedule.query.dto.ScheduleListQueryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleQueryRepository extends JpaRepository<Schedule, Long> {

    @Query("select new dev.gmelon.plango.domain.schedule.query.dto.ScheduleCountQueryDto(s.date, sum(case when s.done = true then 1 else 0 end), count(s)) " +
            "from Schedule s join s.scheduleMembers sm " +
            "where sm.member.id = :memberId " +
            "and s.date >= :startDate " +
            "and s.date <= :endDate " +
            "group by s.date " +
            "having count(s) > 0 " +
            "order by s.date")
    List<ScheduleCountQueryDto> countOfDaysByMemberId(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("select new dev.gmelon.plango.domain.schedule.query.dto.ScheduleListQueryDto(" +
                "s.id, s.title, s.content, s.date, s.startTime, s.endTime, s.scheduleMemberCount," +
                "s.latitude, s.longitude, s.roadAddress, s.placeName, s.done) " +
            "from Schedule s join s.scheduleMembers sm " +
            "where sm.member.id = :memberId " +
            "and s.date = :date " +
            "order by case when s.startTime is null then 0 else 1 end, " +
            "case when s.startTime is null then s.modifiedDate else s.startTime end asc, " +
            "s.endTime asc")
    List<ScheduleListQueryDto> findAllByMemberIdAndDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    @Query("select count(s) from Schedule s " +
            "join s.scheduleMembers sm " +
            "where sm.member.id = :memberId")
    int countByMemberId(@Param("memberId") Long memberId);

}
