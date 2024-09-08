package dev.gmelon.plango.domain.scheduleplace.entity;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gmelon.plango.domain.member.entity.Member;
import org.junit.jupiter.api.Test;

class SchedulePlaceTest {
    @Test
    void 장소를_확정한다() {
        // given
        SchedulePlace schedulePlace = SchedulePlace.builder()
                .confirmed(false)
                .build();
        
        // when
        schedulePlace.confirm();
        
        // then
        assertThat(schedulePlace.isConfirmed()).isTrue();
    }

    @Test
    void 장소의_확정을_취소한다() {
        // given
        SchedulePlace schedulePlace = SchedulePlace.builder()
                .confirmed(true)
                .build();

        // when
        schedulePlace.deny();

        // then
        assertThat(schedulePlace.isConfirmed()).isFalse();
    }

    @Test
    void 장소의_정보를_수정한다() {
        // given
        SchedulePlace schedulePlace = SchedulePlace.builder()
                .memo("장소 메모")
                .category("장소 카테고리")
                .build();
        SchedulePlaceEditor editor = SchedulePlaceEditor.builder()
                .memo("새로운 메모")
                .category("새로운 카테고리")
                .build();

        // when
        schedulePlace.edit(editor);

        // then
        assertThat(schedulePlace.getMemo()).isEqualTo(editor.getMemo());
        assertThat(schedulePlace.getCategory()).isEqualTo(editor.getCategory());
    }

    @Test
    void 주어진_회원이_장소에_좋아요를_남기면_SchedulePlaceLike가_추가된다() {
        // given
        Member member = Member.builder().id(1L).build();
        SchedulePlace schedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .build();

        // when
        schedulePlace.like(member);

        // then
        assertThat(schedulePlace.getSchedulePlaceLikes()).hasSize(1);
        assertThat(schedulePlace.getSchedulePlaceLikes()).extracting(SchedulePlaceLike::memberId).containsOnly(1L);
    }

    @Test
    void 주어진_회원이_장소_좋아요를_취소하면_SchedulePlaceLike가_삭제된다() {
        // given
        Member member = Member.builder().id(1L).build();
        SchedulePlace schedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .build();

        schedulePlace.like(member);

        // when
        schedulePlace.dislike(member.getId());

        // then
        assertThat(schedulePlace.getSchedulePlaceLikes()).hasSize(0);
    }
}
