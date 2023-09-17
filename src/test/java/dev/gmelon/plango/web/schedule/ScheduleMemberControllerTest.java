package dev.gmelon.plango.web.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.schedule.dto.ScheduleMemberAddRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class ScheduleMemberControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleMemberRepository scheduleMemberRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MemberRepository memberRepository;

    @PlangoMockUser
    @Test
    void 멤버_추가() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        ScheduleMember ownerMember = ScheduleMember.createOwner(member, givenSchedule);
        givenSchedule.setScheduleMembers(List.of(ownerMember));
        scheduleRepository.save(givenSchedule);

        ScheduleMemberAddRequestDto request = ScheduleMemberAddRequestDto.builder()
                .memberId(anotherMember.getId())
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/" + givenSchedule.getId() + "/members")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ScheduleMember addedScheduleMember = assertDoesNotThrow(() ->
                scheduleMemberRepository.findByMemberIdAndScheduleId(anotherMember.getId(), givenSchedule.getId()).get());
        assertThat(addedScheduleMember.isAccepted()).isFalse();
        assertThat(addedScheduleMember.isOwner()).isFalse();
    }

    @PlangoMockUser
    @Test
    void 일정_소유자가_아닌_회원이_멤버_추가시_예외_발생() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        ScheduleMember ownerMember = ScheduleMember.createOwner(anotherMember, givenSchedule);
        givenSchedule.setScheduleMembers(List.of(ownerMember));
        scheduleRepository.save(givenSchedule);

        ScheduleMemberAddRequestDto request = ScheduleMemberAddRequestDto.builder()
                .memberId(member.getId())
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/" + givenSchedule.getId() + "/members")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @PlangoMockUser
    @Test
    void 이미_존재하는_멤버_추가시_예외_발생() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(member, givenSchedule),
                ScheduleMember.createParticipant(anotherMember, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        ScheduleMemberAddRequestDto request = ScheduleMemberAddRequestDto.builder()
                .memberId(anotherMember.getId())
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/" + givenSchedule.getId() + "/members")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 멤버_삭제() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(member, givenSchedule),
                ScheduleMember.createParticipant(anotherMember, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + givenSchedule.getId() + "/members/" + anotherMember.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(scheduleMemberRepository.findByMemberIdAndScheduleId(anotherMember.getId(), givenSchedule.getId()))
                .isEmpty();
    }

    @PlangoMockUser
    @Test
    void 일정_소유자가_아닌_회원이_멤버_삭제시_예외_발생() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(anotherMember, givenSchedule),
                ScheduleMember.createParticipant(member, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + givenSchedule.getId() + "/members/" + anotherMember.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @PlangoMockUser
    @Test
    void 일정에_참여하지_않는_멤버_삭제시_예외_발생() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(member, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + givenSchedule.getId() + "/members/" + anotherMember.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 일정의_소유자가_자기자신을_삭제시_예외_발생() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(member, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + givenSchedule.getId() + "/members/" + member.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 일정_초대_수락() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(anotherMember, givenSchedule),
                ScheduleMember.createParticipant(member, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/schedules/" + givenSchedule.getId() + "/members/accept")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ScheduleMember scheduleMember = scheduleMemberRepository.findByMemberIdAndScheduleId(member.getId(), givenSchedule.getId()).get();
        assertThat(scheduleMember.isAccepted()).isTrue();
    }

    @PlangoMockUser
    @Test
    void 초대받지않은_일정_초대_수락() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/schedules/" + givenSchedule.getId() + "/members/accept")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 일정_초대_거절_또는_일정_나가기() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(anotherMember, givenSchedule),
                ScheduleMember.createParticipant(member, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + givenSchedule.getId() + "/members")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(scheduleMemberRepository.findByMemberIdAndScheduleId(member.getId(), givenSchedule.getId())).isEmpty();
    }

    @PlangoMockUser
    @Test
    void 일정_소유자가_일정_나가기_시_예외_발생() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(member, givenSchedule),
                ScheduleMember.createParticipant(anotherMember, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + givenSchedule.getId() + "/members")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(scheduleMemberRepository.findByMemberIdAndScheduleId(member.getId(), givenSchedule.getId())).isPresent();
    }

    @PlangoMockUser
    @Test
    void 초대받지않은_일정_초대_거절_또는_일정_나가기() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정 제목")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(anotherMember, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/" + givenSchedule.getId() + "/members")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private Member createAnotherMember() {
        Member member = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .build();
        return memberRepository.save(member);
    }
}
