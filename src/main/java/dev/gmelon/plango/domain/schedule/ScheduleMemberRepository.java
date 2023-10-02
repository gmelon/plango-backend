package dev.gmelon.plango.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleMemberRepository extends JpaRepository<ScheduleMember, Long> {

    @Query("select sm from ScheduleMember sm " +
            "join fetch sm.schedule " +
            "where sm.member.id = :memberId " +
            "and sm.schedule.id = :scheduleId ")
    Optional<ScheduleMember> findByMemberIdAndScheduleId(@Param("memberId") Long memberId, @Param("scheduleId") Long scheduleId);

    @Modifying
    @Query("delete from ScheduleMember sm " +
            "where sm.schedule.id in :scheduleIds")
    void deleteAllByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

    void deleteAllByMemberId(Long memberId);
}
