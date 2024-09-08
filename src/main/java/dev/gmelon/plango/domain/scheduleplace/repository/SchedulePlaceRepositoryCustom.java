package dev.gmelon.plango.domain.scheduleplace.repository;

import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlace;
import java.util.List;

public interface SchedulePlaceRepositoryCustom {

    List<SchedulePlace> search(Long memberId, String trimmedQuery, int page);

}
