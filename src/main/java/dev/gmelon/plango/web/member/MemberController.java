package dev.gmelon.plango.web.member;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.member.MemberService;
import dev.gmelon.plango.service.member.dto.MemberEditNameRequestDto;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberStatisticsResponseDto;
import dev.gmelon.plango.service.member.dto.PasswordChangeRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/profile")
    public MemberProfileResponseDto getMyProfile(@LoginMember Long memberId) {
        return memberService.getMyProfile(memberId);
    }

    @GetMapping("/statistics")
    public MemberStatisticsResponseDto getMyStatistics(@LoginMember Long memberId) {
        return memberService.getMyStatistics(memberId);
    }

    @PatchMapping("/password")
    public void changePassword(@LoginMember Long memberId,
                               @RequestBody @Valid PasswordChangeRequestDto requestDto) {
        memberService.changePassword(memberId, requestDto);
    }

    @PatchMapping("/name")
    public void editName(@LoginMember Long memberId,
                     @RequestBody @Valid MemberEditNameRequestDto requestDto) {
        memberService.editName(memberId, requestDto);
    }

}
