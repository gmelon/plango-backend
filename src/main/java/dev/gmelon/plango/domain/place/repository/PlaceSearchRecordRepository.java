package dev.gmelon.plango.domain.place.repository;

import dev.gmelon.plango.domain.place.entity.PlaceSearchRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceSearchRecordRepository extends JpaRepository<PlaceSearchRecord, Long>, PlaceSearchRecordRepositoryCustom {

    Optional<PlaceSearchRecord> findByKeywordAndMemberId(String keyword, Long memberId);

    void deleteAllInBatchByMemberId(Long memberId);

}
