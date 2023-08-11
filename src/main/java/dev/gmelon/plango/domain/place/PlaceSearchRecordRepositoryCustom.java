package dev.gmelon.plango.domain.place;

import java.util.List;

public interface PlaceSearchRecordRepositoryCustom {

    List<PlaceSearchRecord> findAllByMemberId(Long memberId, int page);

}
