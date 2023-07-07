package dev.gmelon.plango.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByMemberId(Long memberId);

    // TODO 더 좋은 방법이 있을거같은데.. 찾아보기
    List<Schedule> findByMemberIdAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(Long memberId, LocalDateTime start, LocalDateTime end);

    List<Schedule> findByMemberIdAndStartTimeBetweenAndDiaryNotNullOrderByStartTimeAscEndTimeAsc(Long memberId, LocalDateTime start, LocalDateTime end);

    List<Schedule> findByMemberIdAndStartTimeBetweenAndDiaryNullOrderByStartTimeAscEndTimeAsc(Long memberId, LocalDateTime start, LocalDateTime end);

    Optional<Schedule> findByDiaryId(Long diaryId);

}
