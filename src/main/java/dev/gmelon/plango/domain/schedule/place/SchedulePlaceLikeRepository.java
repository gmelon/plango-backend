package dev.gmelon.plango.domain.schedule.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulePlaceLikeRepository extends JpaRepository<SchedulePlaceLike, Long> {

    Optional<SchedulePlaceLike> findBySchedulePlaceIdAndMemberId(Long schedulePlaceId, Long memberId);

    int countBySchedulePlaceIdAndMemberId(Long schedulePlaceId, Long memberId);

}
