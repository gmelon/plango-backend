package dev.gmelon.plango.domain.schedule.repository;

import dev.gmelon.plango.domain.schedule.entity.Schedule;
import java.util.List;

public interface ScheduleRepositoryCustom {

    List<Schedule> search(Long memberId, String trimmedQuery, int page);

}
