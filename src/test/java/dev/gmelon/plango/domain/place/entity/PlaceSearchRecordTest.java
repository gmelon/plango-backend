package dev.gmelon.plango.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gmelon.plango.domain.member.entity.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PlaceSearchRecordTest {
    @Test
    void 장소_검색_시_lastSearchedDate이_주어진_시간으로_변경된다() {
        // given
        LocalDateTime oldDateTime = LocalDateTime.of(2023, 10, 1, 0, 0, 0);
        LocalDateTime newDateTime = LocalDateTime.of(2023, 10, 10, 0, 0, 0);
        PlaceSearchRecord placeSearchRecord = PlaceSearchRecord.builder()
                .lastSearchedDate(oldDateTime)
                .build();

        // when
        placeSearchRecord.search(newDateTime);

        // then
        assertThat(placeSearchRecord.getLastSearchedDate()).isEqualTo(newDateTime);
    }

    @Test
    void 검색한_회원의_id를_반환한다() {
        // given
        Member member = Member.builder()
                .id(1L)
                .build();
        PlaceSearchRecord placeSearchRecord = PlaceSearchRecord.builder()
                .member(member)
                .build();

        // when, then
        assertThat(placeSearchRecord.memberId()).isEqualTo(member.getId());
    }
}
