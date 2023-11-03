package dev.gmelon.plango.domain.diary;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom {

    @Query("select distinct d from Diary d left outer join fetch d.diaryImages " +
            "where d.id = :id")
    Optional<Diary> findById(@Param("id") Long id);

    @Query("select distinct d from Diary d left outer join fetch d.diaryImages join fetch d.schedule " +
            "where d.id = :id")
    Optional<Diary> findByIdWithSchedule(@Param("id") Long diaryId);

    @Query("select distinct d from Diary d left outer join fetch d.diaryImages " +
            "where d.schedule.id = :scheduleId")
    List<Diary> findAllByScheduleId(@Param("scheduleId") Long scheduleId);

    List<Diary> findAllByMemberId(Long memberId);

    List<Diary> findAllByScheduleIdIn(List<Long> scheduleIds);

    @Query("select distinct d from Diary d left outer join fetch d.diaryImages join fetch d.schedule " +
            "where d.schedule.id = :scheduleId " +
            "and d.member.id = :memberId")
    Optional<Diary> findByMemberIdAndScheduleId(@Param("memberId") Long memberId, @Param("scheduleId") Long scheduleId);

    @Query("select distinct d from Diary d left outer join fetch d.diaryImages " +
            "join fetch d.schedule s " +
            "where d.member.id = :memberId " +
            "and s.date = :date " +
            "order by case when s.startTime is null then 0 else 1 end, " +
            "case when s.startTime is null then s.modifiedDate else s.startTime end asc, " +
            "s.endTime asc")
    List<Diary> findAllByMemberIdAndDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    Optional<Diary> findByContent(String content);

    void deleteAllInBatchByMemberId(Long memberId);
}
