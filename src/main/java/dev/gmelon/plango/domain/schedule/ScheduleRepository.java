package dev.gmelon.plango.domain.schedule;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("select distinct s from Schedule s " +
            "join fetch s.scheduleMembers sm " +
            "join fetch sm.member " +
            "where s.id = :id")
    Optional<Schedule> findByIdWithScheduleMembers(@Param("id") Long scheduleId);

    @Query("select s from Schedule s " +
            "join fetch s.scheduleMembers sm " +
            "join fetch sm.member " +
            "where s.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    Optional<Schedule> findByIdWithScheduleMembersWithLock(@Param("id") Long scheduleId);

    @Query("select s.id from Schedule s join s.scheduleMembers sm " +
            "where sm.member.id = :memberId " +
            "and sm.owner = true")
    List<Long> findAllIdsByOwnerMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("delete from Schedule s " +
            "where s.id in :scheduleIds")
    void deleteAllByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

}
