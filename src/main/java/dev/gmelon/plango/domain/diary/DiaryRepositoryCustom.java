package dev.gmelon.plango.domain.diary;

import java.util.List;

public interface DiaryRepositoryCustom {

    List<Diary> search(Long memberId, String trimmedQuery, int page);

}
