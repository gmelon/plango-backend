package dev.gmelon.plango.service.schedule;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.schedule.*;
import dev.gmelon.plango.service.schedule.dto.ScheduleMemberAddRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class ScheduleMemberServiceTest {

    private Member memberA;
    private Member memberB;

    @Autowired
    private ScheduleMemberService scheduleMemberService;
    @Autowired
    private ScheduleMemberRepository scheduleMemberRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberA = Member.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberA);

        memberB = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberB);
    }

    @Test
    void 멤버_추가() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        ScheduleMember ownerMember = ScheduleMember.createOwner(memberA, givenSchedule);
        givenSchedule.setScheduleMembers(List.of(ownerMember));
        scheduleRepository.save(givenSchedule);

        ScheduleMemberAddRequestDto request = ScheduleMemberAddRequestDto.builder()
                .memberId(memberB.getId())
                .build();

        // when
        scheduleMemberService.invite(memberA.getId(), givenSchedule.getId(), request);

        // then
        ScheduleMember addedScheduleMember = assertDoesNotThrow(() ->
                scheduleMemberRepository.findByMemberIdAndScheduleId(memberB.getId(), givenSchedule.getId()).get());
        assertThat(addedScheduleMember.isAccepted()).isFalse();
        assertThat(addedScheduleMember.isOwner()).isFalse();
        assertThat(scheduleRepository.findById(givenSchedule.getId()).get().getScheduleMemberCount()).isEqualTo(2);
    }

    @Test
    void 일정_소유자가_아닌_회원이_멤버_추가시_예외_발생() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        ScheduleMember ownerMember = ScheduleMember.createOwner(memberB, givenSchedule);
        givenSchedule.setScheduleMembers(List.of(ownerMember));
        scheduleRepository.save(givenSchedule);

        ScheduleMemberAddRequestDto request = ScheduleMemberAddRequestDto.builder()
                .memberId(memberA.getId())
                .build();

        // when, then
        assertThatThrownBy(() -> scheduleMemberService.invite(memberA.getId(), givenSchedule.getId(), request))
                .isInstanceOf(NoOwnerOfScheduleException.class);
    }

    @Test
    void 이미_존재하는_멤버_추가시_예외_발생() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(memberA, givenSchedule),
                ScheduleMember.createParticipant(memberB, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        ScheduleMemberAddRequestDto request = ScheduleMemberAddRequestDto.builder()
                .memberId(memberB.getId())
                .build();

        // when, then
        assertThatThrownBy(() -> scheduleMemberService.invite(memberA.getId(), givenSchedule.getId(), request))
                .isInstanceOf(DuplicateScheduleMemberException.class);
    }

    @Test
    void 멤버_삭제() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(memberA, givenSchedule),
                ScheduleMember.createParticipant(memberB, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        scheduleMemberService.remove(memberA.getId(), givenSchedule.getId(), memberB.getId());

        // then
        assertThat(scheduleMemberRepository.findByMemberIdAndScheduleId(memberB.getId(), givenSchedule.getId()))
                .isEmpty();
        assertThat(scheduleRepository.findById(givenSchedule.getId()).get().getScheduleMemberCount()).isEqualTo(1);
    }

    @Test
    void 일정_소유자가_아닌_회원이_멤버_삭제시_예외_발생() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(memberB, givenSchedule),
                ScheduleMember.createParticipant(memberA, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when, then
        assertThatThrownBy(() -> scheduleMemberService.remove(memberA.getId(), givenSchedule.getId(), memberB.getId()))
                .isInstanceOf(NoOwnerOfScheduleException.class);
    }

    @Test
    void 일정에_참여하지_않는_멤버_삭제시_예외_발생() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(memberA, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when, then
        assertThatThrownBy(() -> scheduleMemberService.remove(memberA.getId(), givenSchedule.getId(), memberB.getId()))
                .isInstanceOf(NoSuchScheduleMemberException.class);
    }

    @Test
    void 일정의_소유자가_자기자신을_삭제시_예외_발생() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(memberA, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when, then
        assertThatThrownBy(() -> scheduleMemberService.remove(memberA.getId(), givenSchedule.getId(), memberA.getId()))
                .isInstanceOf(DeleteOwnerOfSchduleException.class);
    }

    @Test
    void 일정_초대_수락() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(memberA, givenSchedule),
                ScheduleMember.createParticipant(memberB, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        scheduleMemberService.acceptInvitation(memberB.getId(), givenSchedule.getId());

        // then
        ScheduleMember scheduleMember = scheduleMemberRepository.findByMemberIdAndScheduleId(memberB.getId(), givenSchedule.getId()).get();
        assertThat(scheduleMember.isAccepted()).isTrue();
    }

    @Test
    void 초대받지않은_일정_초대_수락() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(memberA);
        scheduleRepository.save(givenSchedule);

        // when, then
        assertThatThrownBy(() -> scheduleMemberService.acceptInvitation(memberB.getId(), givenSchedule.getId()))
                .isInstanceOf(UnInvitedMemberException.class);
    }

    @Test
    void 일정_초대_거절_또는_일정_나가기() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(memberA, givenSchedule),
                ScheduleMember.createParticipant(memberB, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        scheduleMemberService.rejectOrExitSchedule(memberB.getId(), givenSchedule.getId());

        // then
        assertThat(scheduleMemberRepository.findByMemberIdAndScheduleId(memberB.getId(), givenSchedule.getId())).isEmpty();
    }

    @Test
    void 일정_소유자가_일정_나가기_시_예외_발생() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(memberA);
        scheduleRepository.save(givenSchedule);

        // when, then
        assertThatThrownBy(() -> scheduleMemberService.rejectOrExitSchedule(memberA.getId(), givenSchedule.getId()))
                .isInstanceOf(DeleteOwnerOfSchduleException.class);
    }

    @Test
    void 초대받지않은_일정_초대_거절_또는_일정_나가기() {
        // given
        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(memberA);
        scheduleRepository.save(givenSchedule);

        // when, then
        assertThatThrownBy(() -> scheduleMemberService.rejectOrExitSchedule(memberB.getId(), givenSchedule.getId()))
                .isInstanceOf(UnInvitedMemberException.class);
    }
}
