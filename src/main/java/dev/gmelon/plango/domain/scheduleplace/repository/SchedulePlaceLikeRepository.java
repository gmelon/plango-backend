package dev.gmelon.plango.domain.scheduleplace.repository;

import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlaceLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulePlaceLikeRepository extends JpaRepository<SchedulePlaceLike, Long> {

    Optional<SchedulePlaceLike> findBySchedulePlaceIdAndMemberId(Long schedulePlaceId, Long memberId);

    int countBySchedulePlaceIdAndMemberId(Long schedulePlaceId, Long memberId);

    void deleteAllInBatchByMemberId(Long memberId);

}
