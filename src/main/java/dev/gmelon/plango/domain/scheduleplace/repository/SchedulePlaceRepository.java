package dev.gmelon.plango.domain.scheduleplace.repository;

import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulePlaceRepository extends JpaRepository<SchedulePlace, Long>, SchedulePlaceRepositoryCustom {

    List<SchedulePlace> findAllByScheduleId(Long scheduleId);

}
