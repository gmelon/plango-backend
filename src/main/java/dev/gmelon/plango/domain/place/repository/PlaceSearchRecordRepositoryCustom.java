package dev.gmelon.plango.domain.place.repository;

import dev.gmelon.plango.domain.place.entity.PlaceSearchRecord;
import java.util.List;

public interface PlaceSearchRecordRepositoryCustom {

    List<PlaceSearchRecord> findAllByMemberId(Long memberId, int page);

}
