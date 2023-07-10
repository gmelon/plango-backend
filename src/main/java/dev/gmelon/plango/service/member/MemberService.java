package dev.gmelon.plango.service.member;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberStatisticsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;


    public MemberProfileResponseDto getMyProfile(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberProfileResponseDto.from(member);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public MemberStatisticsResponseDto getMyStatistics(Long memberId) {
        int scheduleCount = (int) scheduleRepository.countByMemberId(memberId);
        int doneScheduleCount = (int) scheduleRepository.countByMemberIdAndDoneIsTrue(memberId);
        int diaryCount = (int) scheduleRepository.countByMemberIdAndDiaryNotNull(memberId);

        return MemberStatisticsResponseDto.builder()
                .scheduleCount(scheduleCount)
                .doneScheduleCount(doneScheduleCount)
                .diaryCount(diaryCount)
                .build();
    }
}
