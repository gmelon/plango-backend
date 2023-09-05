package dev.gmelon.plango.web.member;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.member.MemberService;
import dev.gmelon.plango.service.member.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/profile")
    public MemberProfileResponseDto getMyProfile(@LoginMember Long memberId) {
        return memberService.getMyProfile(memberId);
    }

    @GetMapping("/{targetMemberId}/profile")
    public MemberProfileResponseDto getProfile(
            @LoginMember Long currentMemberId,
            @PathVariable Long targetMemberId) {
        return memberService.getProfile(currentMemberId, targetMemberId);
    }

    @GetMapping
    public List<MemberSearchResponseDto> searchWithoutCurrentMember(
            @LoginMember Long currentMemberId,
            @ModelAttribute @Valid MemberSearchRequestDto requestDto) {
        return memberService.searchWithoutCurrentMember(currentMemberId, requestDto);
    }

    @PatchMapping("/profile")
    public void editProfile(@LoginMember Long memberId,
                            @RequestBody @Valid MemberEditProfileRequestDto requestDto) {
        memberService.editProfile(memberId, requestDto);
    }

    @PatchMapping("/password")
    public void changePassword(@LoginMember Long memberId,
                               @RequestBody @Valid PasswordChangeRequestDto requestDto) {
        memberService.changePassword(memberId, requestDto);
    }

}
