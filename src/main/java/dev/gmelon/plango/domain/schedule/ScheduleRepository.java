package dev.gmelon.plango.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByMemberId(Long memberId);

    // TODO 리팩토링
    List<Schedule> findByMemberIdAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(Long memberId, LocalDateTime start, LocalDateTime end);

    List<Schedule> findByMemberIdAndStartTimeBetweenAndDiaryNotNullOrderByStartTimeAscEndTimeAsc(Long memberId, LocalDateTime start, LocalDateTime end);

    List<Schedule> findByMemberIdAndStartTimeBetweenAndDiaryNullOrderByStartTimeAscEndTimeAsc(Long memberId, LocalDateTime start, LocalDateTime end);

    Optional<Schedule> findByDiaryId(Long diaryId);

    long countByMemberId(Long memberId);

    long countByMemberIdAndDoneIsTrue(Long memberId);

    long countByMemberIdAndDiaryNotNull(Long memberId);

}
