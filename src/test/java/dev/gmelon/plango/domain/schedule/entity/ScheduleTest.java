package dev.gmelon.plango.domain.schedule.entity;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlace;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScheduleTest {
    private static final Member MEMBER_A = Member.builder().id(1L).build();
    private static final Member MEMBER_B = Member.builder().id(2L).build();

    @Test
    void 주어진_회원이_Schedule의_member인지_확인한다() {
        // given
        Schedule schedule = new Schedule();
        schedule.setScheduleMembers(List.of(ScheduleMember.createParticipant(MEMBER_A, schedule)));

        // when, then
        assertThat(schedule.isMember(MEMBER_A.getId())).isTrue();
        assertThat(schedule.isMember(MEMBER_B.getId())).isFalse();
    }

    @Test
    void 주어진_회원이_Schedule의_owner인지_확인한다() {
        // given
        Schedule schedule = new Schedule();
        schedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(MEMBER_A, schedule),
                ScheduleMember.createParticipant(MEMBER_B, schedule)
        ));

        // when, then
        assertThat(schedule.isOwner(MEMBER_A.getId())).isTrue();
        assertThat(schedule.isOwner(MEMBER_B.getId())).isFalse();
    }

    @Test
    void 주어진_회원이_Schedule을_수락했는지_확인한다() {
        // given
        Schedule schedule = new Schedule();
        schedule.setScheduleMembers(List.of(
                ScheduleMember.builder().member(MEMBER_A).schedule(schedule).accepted(true).build(),
                ScheduleMember.builder().member(MEMBER_B).schedule(schedule).accepted(false).build()
        ));

        // when, then
        assertThat(schedule.isAccepted(MEMBER_A.getId())).isTrue();
        assertThat(schedule.isAccepted(MEMBER_B.getId())).isFalse();
    }

    @Test
    void Schedule의_ownerId를_반환한다() {
        // given
        Schedule schedule = new Schedule();
        schedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(MEMBER_A, schedule),
                ScheduleMember.createParticipant(MEMBER_B, schedule)
        ));

        // when, then
        assertThat(schedule.ownerId()).isEqualTo(MEMBER_A.getId());
    }

    @Test
    void Schedule을_수정한다() {
        // given
        Schedule schedule = Schedule.builder()
                .title("이전 기록")
                .content("이전 내용")
                .date(LocalDate.of(2023, 10, 1))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(20, 0, 0))
                .build();
        ScheduleEditor editor = ScheduleEditor.builder()
                .title("새로운 기록")
                .content("새로운 내용")
                .date(LocalDate.of(2023, 10, 30))
                .startTime(LocalTime.of(13, 0, 0))
                .endTime(LocalTime.of(23, 0, 0))
                .build();

        // when
        schedule.edit(editor);

        // then
        assertThat(schedule)
                .usingRecursiveComparison()
                .ignoringFields("id", "done", "diaries", "schedulePlaces", "scheduleMembers", "createdTime", "modifiedDate")
                .isEqualTo(editor);
    }

    @ParameterizedTest
    @CsvSource(value = {"false:true", "true:false", "true:true", "false:false"}, delimiter = ':')
    void 완료_여부를_변경한다(boolean givenDone, boolean expectedDone) {
        // given
        Schedule schedule = Schedule.builder()
                .done(givenDone)
                .build();

        // when
        schedule.changeDone(expectedDone);

        // then
        assertThat(schedule.isDone()).isEqualTo(expectedDone);
    }

    @Test
    void Schedule에서_회원을_탈퇴시킨다() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획")
                .build();
        List<ScheduleMember> scheduleMembers = new ArrayList<>(List.of(
                ScheduleMember.createOwner(MEMBER_A, schedule),
                ScheduleMember.createParticipant(MEMBER_B, schedule)
        ));
        schedule.setScheduleMembers(scheduleMembers);

        // when
        schedule.deleteScheduleMember(scheduleMembers.get(1));

        // then
        assertThat(schedule.getScheduleMembers()).hasSize(1);
        assertThat(schedule.getScheduleMembers()).extracting(ScheduleMember::memberId).containsOnly(MEMBER_A.getId());
    }

    @Test
    void Schedule에_장소를_추가한다() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획")
                .build();
        SchedulePlace schedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .build();

        // when
        schedule.addSchedulePlace(schedulePlace);

        // then
        assertThat(schedule.getSchedulePlaces()).containsOnly(schedulePlace);
    }

    @Test
    void Schedule에서_장소를_삭제한다() {
        // given
        Schedule schedule = Schedule.builder()
                .title("계획")
                .build();
        List<SchedulePlace> schedulePlaces = List.of(
                SchedulePlace.builder()
                        .placeName("장소 1")
                        .build(),
                SchedulePlace.builder()
                        .placeName("장소 2")
                        .build()
        );
        schedule.setSchedulePlaces(new ArrayList<>(schedulePlaces));

        // when
        schedule.removeSchedulePlace(schedulePlaces.get(1));

        // then
        assertThat(schedule.getSchedulePlaces()).hasSize(1);
        assertThat(schedule.getSchedulePlaces()).containsOnly(schedulePlaces.get(0));
    }
}
