package dev.gmelon.plango.domain.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceSearchRecordRepository extends JpaRepository<PlaceSearchRecord, Long>, PlaceSearchRecordRepositoryCustom {

    Optional<PlaceSearchRecord> findByKeywordAndMemberId(String keyword, Long memberId);

}
