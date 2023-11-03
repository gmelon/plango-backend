package dev.gmelon.plango.service.auth;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.domain.place.PlaceSearchRecordRepository;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.place.SchedulePlaceLikeRepository;
import dev.gmelon.plango.exception.member.DuplicateEmailException;
import dev.gmelon.plango.exception.member.DuplicateNicknameException;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMemberRepository scheduleMemberRepository;
    private final S3Repository s3Repository;
    private final PasswordEncoder passwordEncoder;
    private final DiaryRepository diaryRepository;
    private final PlaceSearchRecordRepository placeSearchRecordRepository;
    private final NotificationRepository notificationRepository;
    private final FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;
    private final SchedulePlaceLikeRepository schedulePlaceLikeRepository;

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
            throw new DuplicateEmailException();
        }
    }

    private void validateNicknameIsUnique(SignupRequestDto requestDto) {
        boolean isNicknameAlreadyExists = memberRepository.findByNickname(requestDto.getNickname())
                .isPresent();
        if (isNicknameAlreadyExists) {
            throw new DuplicateNicknameException();
        }
    }

    @Transactional
    public void signout(Long memberId) {
        placeSearchRecordRepository.deleteAllInBatchByMemberId(memberId);
        notificationRepository.deleteAllInBatchByMemberId(memberId);
        firebaseCloudMessageTokenRepository.deleteAllInBatchByMemberId(memberId);

        deleteProfileImage(memberId);
        List<Long> scheduleIds = scheduleRepository.findAllIdsByOwnerMemberId(memberId);
        deleteDiaries(scheduleIds, memberId);
        scheduleRepository.deleteAllInBatchByIdIn(scheduleIds);

        scheduleMemberRepository.deleteAllInBatchByMemberId(memberId);
        schedulePlaceLikeRepository.deleteAllInBatchByMemberId(memberId);

        memberRepository.deleteById(memberId);
    }

    private void deleteDiaries(List<Long> scheduleIds, Long memberId) {
        deleteAllDiaryImages(scheduleIds);
        diaryRepository.deleteAllInBatchByMemberId(memberId);
    }

    private void deleteAllDiaryImages(List<Long> scheduleIds) {
        List<String> diaryImageUrls = diaryRepository.findAllByScheduleIds(scheduleIds).stream()
                .flatMap(diary -> diary.getDiaryImageUrls().stream())
                .collect(toList());

        s3Repository.deleteAll(diaryImageUrls);
    }

    private void deleteProfileImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);

        String profileImageUrl = member.getProfileImageUrl();
        if (profileImageUrl != null) {
            s3Repository.delete(profileImageUrl);
        }
    }
}
