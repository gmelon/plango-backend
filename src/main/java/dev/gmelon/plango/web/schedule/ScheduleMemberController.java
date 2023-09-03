package dev.gmelon.plango.web.schedule;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.schedule.ScheduleMemberService;
import dev.gmelon.plango.service.schedule.dto.ScheduleMemberAddRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
