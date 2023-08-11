package dev.gmelon.plango.domain.schedule;

import dev.gmelon.plango.service.schedule.dto.ScheduleCountResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);

    // TODO 얘도 fetch join 하도록 수정
    Optional<Schedule> findByDiaryId(Long diaryId);

    long countByMemberId(Long memberId);

    long countByMemberIdAndDoneIsTrue(Long memberId);

    long countByMemberIdAndDiaryNotNull(Long memberId);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.member.id = :memberId " +
            "AND s.date = :date " +
            "ORDER BY CASE WHEN s.startTime IS NULL THEN 0 ELSE 1 END, " +
            "CASE WHEN s.startTime IS NULL THEN s.modifiedDate ELSE s.startTime END ASC, " +
            "s.endTime ASC")
    List<Schedule> findByMemberIdAndDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.member.id = :memberId " +
            "AND s.diary IS NULL " +
            "AND s.date = :date " +
            "ORDER BY CASE WHEN s.startTime IS NULL THEN 0 ELSE 1 END, " +
            "CASE WHEN s.startTime IS NULL THEN s.modifiedDate ELSE s.startTime END ASC, " +
            "s.endTime ASC")
    List<Schedule> findByMemberIdAndDateAndDiaryNull(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    // TODO Diary를 바로 반환하도록 수정
    @Query("SELECT distinct s FROM Schedule s join fetch s.diary d left outer join fetch d.diaryImages " +
            "WHERE s.member.id = :memberId " +
            "AND s.date = :date " +
            "ORDER BY CASE WHEN s.startTime IS NULL THEN 0 ELSE 1 END, " +
            "CASE WHEN s.startTime IS NULL THEN s.modifiedDate ELSE s.startTime END ASC, " +
            "s.endTime ASC")
    List<Schedule> findByMemberIdAndDateAndDiaryNotNull(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    @Query("SELECT new dev.gmelon.plango.service.schedule.dto.ScheduleCountResponseDto(s.date, sum(case when s.done = true then 1 else 0 end), count(s)) " +
            "FROM Schedule s " +
            "WHERE s.member.id = :memberId " +
            "AND s.date >= :startDate " +
            "AND s.date <= :endDate " +
            "GROUP BY s.date " +
            "HAVING count(s) > 0 " +
            "ORDER BY s.date")
    List<ScheduleCountResponseDto> findByMemberIdAndCountOfDays(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
