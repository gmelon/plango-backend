package dev.gmelon.plango.domain.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gmelon.plango.domain.member.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleMemberTest {
    private static final Member MEMBER = Member.builder().id(1L).build();
    private static Schedule SCHEDULE;

    @BeforeEach
    void setUp() {
        SCHEDULE = Schedule.builder().title("계획").build();
    }

    @Test
    void 주어진_Schedule의_주인인_member를_생성한다() {
        // when
        ScheduleMember scheduleMember = ScheduleMember.createOwner(MEMBER, SCHEDULE);

        // then
        assertThat(scheduleMember.isAccepted()).isTrue();
        assertThat(scheduleMember.isOwner()).isTrue();
    }

    @Test
    void 주어진_Schedule에_참여하는_member를_생성한다() {
        // when
        ScheduleMember scheduleMember = ScheduleMember.createParticipant(MEMBER, SCHEDULE);

        // then
        assertThat(scheduleMember.isAccepted()).isFalse();
        assertThat(scheduleMember.isOwner()).isFalse();
    }

    @Test
    void 일정_참여를_수락한다() {
        // given
        ScheduleMember scheduleMember = ScheduleMember.createParticipant(MEMBER, SCHEDULE);

        // when
        scheduleMember.accept();

        // then
        assertThat(scheduleMember.isAccepted()).isTrue();
    }

    @Test
    void 회원의_id를_반환한다() {
        // given
        ScheduleMember scheduleMember = ScheduleMember.createOwner(MEMBER, SCHEDULE);

        // when, then
        assertThat(scheduleMember.memberId()).isEqualTo(MEMBER.getId());
    }

    @Test
    void 주어진_회원_id와_자신의_회원_id가_같은지_비교한다() {
        // given
        ScheduleMember scheduleMember = ScheduleMember.createOwner(MEMBER, SCHEDULE);

        // when, then
        assertThat(scheduleMember.isMemberEquals(1L)).isTrue();
        assertThat(scheduleMember.isMemberEquals(2L)).isFalse();
    }
}
