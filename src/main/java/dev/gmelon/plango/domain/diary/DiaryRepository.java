package dev.gmelon.plango.domain.diary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("select distinct d from Diary d left outer join fetch d.diaryImages " +
            "where d.id = :id")
    Optional<Diary> findById(@Param("id") Long id);

    Optional<Diary> findByContent(String content);

}
