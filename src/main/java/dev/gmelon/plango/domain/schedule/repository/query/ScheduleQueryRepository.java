package dev.gmelon.plango.domain.schedule.repository.query;

import dev.gmelon.plango.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleQueryRepository extends JpaRepository<Schedule, Long>, ScheduleQueryRepositoryCustom {

}
