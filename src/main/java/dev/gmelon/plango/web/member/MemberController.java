package dev.gmelon.plango.web.member;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.service.member.MemberService;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberStatisticsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/profile")
    public MemberProfileResponseDto getMyProfile(@LoginMember Member member) {
        return memberService.getMyProfile(member.getId());
    }

    @GetMapping("/statistics")
    public MemberStatisticsResponseDto getMyStatistics(@LoginMember Member member) {
        return memberService.getMyStatistics(member.getId());
    }

}
