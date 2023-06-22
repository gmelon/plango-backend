package dev.gmelon.plango.domain.diary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
}
