package dev.gmelon.plango.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleRepositoryCustom {

    @Query("select distinct s from Schedule s " +
            "join fetch s.scheduleMembers sm " +
            "join fetch sm.member " +
            "where s.id = :id")
    Optional<Schedule> findByIdWithMembers(@Param("id") Long scheduleId);

    @Query("select s.id from Schedule s join s.scheduleMembers sm " +
            "where sm.member.id = :memberId " +
            "and sm.owner = true")
    List<Long> findAllIdsByOwnerMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("delete from Schedule s " +
            "where s.id in :scheduleIds")
    void deleteAllByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

}
