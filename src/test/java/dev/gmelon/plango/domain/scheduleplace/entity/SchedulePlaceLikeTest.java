package dev.gmelon.plango.domain.scheduleplace.entity;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gmelon.plango.domain.member.entity.Member;
import org.junit.jupiter.api.Test;

class SchedulePlaceLikeTest {
    @Test
    void 주어진_회원_id와_SchedulePlace_id가_SchedulePlaceLike와_일치하는지_확인한다() {
        // given
        Member member = Member.builder().id(1L).build();
        SchedulePlace schedulePlace = SchedulePlace.builder().id(1L).build();
        SchedulePlaceLike schedulePlaceLike = SchedulePlaceLike.builder()
                .member(member)
                .schedulePlace(schedulePlace)
                .build();

        // when, then
        assertThat(schedulePlaceLike.isMemberAndPlaceEquals(member.getId(), schedulePlace.getId())).isTrue();
        assertThat(schedulePlaceLike.isMemberAndPlaceEquals(member.getId(), schedulePlace.getId() + 1)).isFalse();
        assertThat(schedulePlaceLike.isMemberAndPlaceEquals(member.getId() + 1, schedulePlace.getId())).isFalse();
    }

    @Test
    void SchedulePlaceLike의_회원_id를_반환한다() {
        // given
        Member member = Member.builder().id(1L).build();
        SchedulePlace schedulePlace = SchedulePlace.builder().id(1L).build();
        SchedulePlaceLike schedulePlaceLike = SchedulePlaceLike.builder()
                .member(member)
                .schedulePlace(schedulePlace)
                .build();

        // when, then
        assertThat(schedulePlaceLike.memberId()).isEqualTo(member.getId());
    }
}
