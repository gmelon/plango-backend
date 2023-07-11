package dev.gmelon.plango.service.member;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.member.dto.MemberEditNameRequestDto;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberStatisticsResponseDto;
import dev.gmelon.plango.service.member.dto.PasswordChangeRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberProfileResponseDto getMyProfile(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberProfileResponseDto.from(member);
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

    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequestDto requestDto) {
        Member member = findMemberById(memberId);
        validatePreviousPassword(requestDto, member);

        String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());
        member.changePassword(encodedNewPassword);
    }

    private void validatePreviousPassword(PasswordChangeRequestDto requestDto, Member member) {
        if (!passwordEncoder.matches(requestDto.getPreviousPassword(), member.getPassword())) {
            throw new IllegalArgumentException("이전 비밀번호가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void editName(Long memberId, MemberEditNameRequestDto requestDto) {
        Member member = findMemberById(memberId);
        member.editName(requestDto.getName());
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }
}
