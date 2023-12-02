package dev.gmelon.plango.service.auth;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.config.auth.jwt.JWTProvider;
import dev.gmelon.plango.config.auth.social.SocialClients;
import dev.gmelon.plango.config.auth.social.dto.SocialAccountResponse;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.notification.NotificationRepository;
import dev.gmelon.plango.domain.place.PlaceSearchRecordRepository;
import dev.gmelon.plango.domain.refreshtoken.RefreshToken;
import dev.gmelon.plango.domain.refreshtoken.RefreshTokenRepository;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.place.SchedulePlaceLikeRepository;
import dev.gmelon.plango.exception.auth.NoSuchRefreshTokenException;
import dev.gmelon.plango.exception.auth.RefreshTokenTheftException;
import dev.gmelon.plango.exception.member.DuplicateEmailException;
import dev.gmelon.plango.exception.member.DuplicateMemberException;
import dev.gmelon.plango.exception.member.DuplicateNicknameException;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.auth.dto.LoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.service.auth.dto.SnsLoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SnsRevokeRequestDto;
import dev.gmelon.plango.service.auth.dto.TokenRefreshRequestDto;
import dev.gmelon.plango.service.auth.dto.TokenResponseDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JWTProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
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
    private final SocialClients socialClients;

    @Transactional
    public TokenResponseDto login(LoginRequestDto requestDto) {
        Authentication authenticate = authenticationManagerBuilder.getObject()
                .authenticate(requestDto.toAuthentication());

        TokenResponseDto responseDto = jwtProvider.createToken(authenticate);
        saveRefreshToken(authenticate.getName(), responseDto.getRefreshToken());

        return responseDto;
    }

    private void saveRefreshToken(String email, String refreshTokenValue) {
        RefreshToken refreshToken = RefreshToken.builder()
                .email(email)
                .tokenValue(refreshTokenValue)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public TokenResponseDto snsLogin(SnsLoginRequestDto requestDto) {
        SocialAccountResponse socialAccountResponse = socialClients.requestAccountResponse(requestDto.getMemberType(),
                requestDto.getToken());

        Optional<Member> memberOptional = memberRepository.findByEmail(socialAccountResponse.getEmail());
        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            // TODO memberType 다를 경우 어떻게 예외 처리 할지
//            validateMemberTypeEquals(requestDto, member);
            TokenResponseDto responseDto = jwtProvider.createToken(member);
            saveRefreshToken(member.getEmail(), responseDto.getRefreshToken());
            return responseDto;
        }

        Member member = requestDto.toEntity(socialAccountResponse);
        memberRepository.save(member);

        TokenResponseDto responseDto = jwtProvider.createToken(member);
        saveRefreshToken(member.getEmail(), responseDto.getRefreshToken());
        return responseDto;
    }

    private void validateMemberTypeEquals(SnsLoginRequestDto requestDto, Member member) {
        if (!member.typeEquals(requestDto.getMemberType())) {
            throw new DuplicateMemberException();
        }
    }

    @Transactional
    public void logout(Long memberId) {
        Member member = findMemberById(memberId);
        refreshTokenRepository.deleteById(member.getEmail());
    }

    public void snsRevoke(SnsRevokeRequestDto requestDto) {
        socialClients.revokeToken(requestDto.getMemberType(), requestDto.getSnsTargetId(), requestDto.getToken());
    }

    @Transactional
    public TokenResponseDto tokenRefresh(TokenRefreshRequestDto requestDto) {
        String email = jwtProvider.parseEmailFromRefreshToken(requestDto.getRefreshToken());
        RefreshToken refreshToken = findRefreshTokenByEmail(email);

        if (!refreshToken.getTokenValue().equals(requestDto.getRefreshToken())) {
            log.warn("Refresh Token 불일치. 토큰 탈취 가능성이 있습니다. 회원 email : {}", email);
            refreshTokenRepository.deleteById(email);
            throw new RefreshTokenTheftException();
        }

        TokenResponseDto tokenResponseDto = createNewToken(email);
        updateRefreshToken(refreshToken, tokenResponseDto);
        return tokenResponseDto;
    }

    private RefreshToken findRefreshTokenByEmail(String email) {
        return refreshTokenRepository.findById(email)
                .orElseThrow(NoSuchRefreshTokenException::new);
    }

    private TokenResponseDto createNewToken(String email) {
        Member member = findMemberByEmail(email);
        return jwtProvider.createToken(member);
    }

    private void updateRefreshToken(RefreshToken refreshToken, TokenResponseDto tokenResponseDto) {
        refreshToken.updateTokenValue(tokenResponseDto.getRefreshToken());
        refreshTokenRepository.save(refreshToken);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(NoSuchMemberException::new);
    }

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        validateUniqueColumns(requestDto);

        String encodePassword = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setPassword(encodePassword);
        memberRepository.save(requestDto.toEntity());
    }

    // TODO 리팩토링
    private void validateUniqueColumns(SignupRequestDto requestDto) {
        validateEmailNotEquals(requestDto.getEmail());
        validateEmailNotEquals(requestDto.getNickname());

        validateNicknameNotEquals(requestDto.getEmail());
        validateNicknameNotEquals(requestDto.getNickname());
    }

    private void validateEmailNotEquals(String value) {
        boolean isEmailAlreadyExists = memberRepository.findByEmail(value)
                .isPresent();
        if (isEmailAlreadyExists) {
            throw new DuplicateEmailException();
        }
    }

    private void validateNicknameNotEquals(String value) {
        boolean isNicknameAlreadyExists = memberRepository.findByNickname(value)
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

    private void deleteProfileImage(Long memberId) {
        Member member = findMemberById(memberId);

        String profileImageUrl = member.getProfileImageUrl();
        if (profileImageUrl != null) {
            s3Repository.delete(profileImageUrl);
        }
    }

    private void deleteDiaries(List<Long> scheduleIds, Long memberId) {
        deleteAllMyScheduleDiaryImages(scheduleIds);
        deleteAllParticipatedScheduleDiaryImages(memberId);
        diaryRepository.deleteAllInBatchByMemberId(memberId);
    }

    private void deleteAllMyScheduleDiaryImages(List<Long> scheduleIds) {
        if (scheduleIds.isEmpty()) {
            return;
        }

        List<String> diaryImageUrls = diaryRepository.findAllByScheduleIdIn(scheduleIds).stream()
                .flatMap(diary -> diary.getDiaryImageUrls().stream())
                .collect(toList());

        s3Repository.deleteAll(diaryImageUrls);
    }

    private void deleteAllParticipatedScheduleDiaryImages(Long memberId) {
        List<String> diaryImageUrls = diaryRepository.findAllByMemberId(memberId).stream()
                .flatMap(diary -> diary.getDiaryImageUrls().stream())
                .collect(toList());

        s3Repository.deleteAll(diaryImageUrls);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
