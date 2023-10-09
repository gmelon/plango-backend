package dev.gmelon.plango.domain.schedule;

import java.util.List;

public interface ScheduleRepositoryCustom {

    List<Schedule> search(Long memberId, String trimmedQuery, int page);

}
