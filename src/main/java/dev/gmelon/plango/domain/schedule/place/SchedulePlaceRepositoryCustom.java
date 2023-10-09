package dev.gmelon.plango.domain.schedule.place;

import java.util.List;

public interface SchedulePlaceRepositoryCustom {

    List<SchedulePlace> search(Long memberId, String trimmedQuery, int page);

}
