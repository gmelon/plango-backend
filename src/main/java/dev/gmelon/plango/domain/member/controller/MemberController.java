package dev.gmelon.plango.domain.member.controller;

import dev.gmelon.plango.domain.member.dto.MemberEditProfileRequestDto;
import dev.gmelon.plango.domain.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.domain.member.dto.MemberSearchRequestDto;
import dev.gmelon.plango.domain.member.dto.MemberSearchResponseDto;
import dev.gmelon.plango.domain.member.dto.PasswordChangeRequestDto;
import dev.gmelon.plango.domain.member.dto.TermsAcceptedResponseDto;
import dev.gmelon.plango.domain.member.service.MemberService;
import dev.gmelon.plango.global.config.auth.LoginMember;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/terms")
    public TermsAcceptedResponseDto termsAccepted(@LoginMember Long memberId) {
        return memberService.termsAccepted(memberId);
    }

    @PatchMapping("/terms")
    public void acceptTerms(@LoginMember Long memberId) {
        memberService.acceptTerms(memberId);
    }

}
