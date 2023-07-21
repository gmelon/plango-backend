package dev.gmelon.plango.service.auth;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final S3Repository s3Repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        validateUniqueColumns(requestDto);

        String encodePassword = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setPassword(encodePassword);
        memberRepository.save(requestDto.toEntity());
    }

    private void validateUniqueColumns(SignupRequestDto requestDto) {
        validateEmailIsUnique(requestDto);
        validateNicknameIsUnique(requestDto);
    }

    private void validateEmailIsUnique(SignupRequestDto requestDto) {
        boolean isEmailAlreadyExists = memberRepository.findByEmail(requestDto.getEmail())
                .isPresent();
        if (isEmailAlreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }
    }

    private void validateNicknameIsUnique(SignupRequestDto requestDto) {
        boolean isNicknameAlreadyExists = memberRepository.findByNickname(requestDto.getNickname())
                .isPresent();
        if (isNicknameAlreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }
    }

    @Transactional
    public void signout(Long memberId) {
        deleteAllDiaryImages(memberId);
        deleteProfileImage(memberId);

        scheduleRepository.deleteAllByMemberId(memberId);
        memberRepository.deleteById(memberId);
    }

    private void deleteAllDiaryImages(Long memberId) {
        List<Schedule> schedules = scheduleRepository.findAllByMemberId(memberId);
        schedules.stream()
                .filter(schedule ->  Objects.nonNull(schedule.getDiary()))
                .map(schedule -> schedule.getDiary().getImageUrl())
                .filter(Objects::nonNull)
                .forEach(s3Repository::delete);
    }

    private void deleteProfileImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        String profileImageUrl = member.getProfileImageUrl();
        if (profileImageUrl != null) {
            s3Repository.delete(profileImageUrl);
        }
    }
}
