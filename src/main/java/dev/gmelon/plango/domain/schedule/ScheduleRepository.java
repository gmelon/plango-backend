package dev.gmelon.plango.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByMemberId(Long memberId);

    List<Schedule> findByMemberIdAndStartTimeBetweenOrderByStartTimeAsc(Long memberId, LocalDateTime start, LocalDateTime end);

}
