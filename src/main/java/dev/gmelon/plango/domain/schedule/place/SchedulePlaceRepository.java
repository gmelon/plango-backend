package dev.gmelon.plango.domain.schedule.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulePlaceRepository extends JpaRepository<SchedulePlace, Long> {

    List<SchedulePlace> findAllByScheduleId(Long scheduleId);

}
