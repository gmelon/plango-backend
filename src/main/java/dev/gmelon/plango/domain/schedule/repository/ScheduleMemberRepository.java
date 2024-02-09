package dev.gmelon.plango.domain.schedule.repository;

import dev.gmelon.plango.domain.schedule.entity.ScheduleMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleMemberRepository extends JpaRepository<ScheduleMember, Long> {

    @Query("select sm from ScheduleMember sm " +
            "join fetch sm.schedule " +
            "where sm.member.id = :memberId " +
            "and sm.schedule.id = :scheduleId ")
    Optional<ScheduleMember> findByMemberIdAndScheduleId(@Param("memberId") Long memberId, @Param("scheduleId") Long scheduleId);

    void deleteAllInBatchByMemberId(Long memberId);
}
