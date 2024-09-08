package dev.gmelon.plango.domain.schedule.controller;

import dev.gmelon.plango.domain.schedule.dto.ScheduleMemberAddRequestDto;
import dev.gmelon.plango.domain.schedule.service.ScheduleMemberService;
import dev.gmelon.plango.global.config.auth.LoginMember;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/schedules/{scheduleId}/members")
@RestController
public class ScheduleMemberController {

    private final ScheduleMemberService scheduleMemberService;

    @PostMapping
    public void invite(@LoginMember Long memberId,
                       @PathVariable Long scheduleId,
                       @RequestBody @Valid ScheduleMemberAddRequestDto requestDto) {
        scheduleMemberService.invite(memberId, scheduleId, requestDto);
    }

    @DeleteMapping("/{targetMemberId}")
    public void remove(@LoginMember Long memberId,
                       @PathVariable Long scheduleId,
                       @PathVariable Long targetMemberId) {
        scheduleMemberService.remove(memberId, scheduleId, targetMemberId);
    }

    @PatchMapping("/accept")
    public void acceptInvitation(@LoginMember Long memberId,
                                 @PathVariable Long scheduleId) {
        scheduleMemberService.acceptInvitation(memberId, scheduleId);
    }

    @DeleteMapping
    public void rejectOrExitSchedule(@LoginMember Long memberId,
                                     @PathVariable Long scheduleId) {
        scheduleMemberService.rejectOrExitSchedule(memberId, scheduleId);
    }

}
