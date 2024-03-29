package dev.gmelon.plango.domain.auth.service;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.auth.dto.CheckEmailTokenRequestDto;
import dev.gmelon.plango.domain.auth.dto.LoginRequestDto;
import dev.gmelon.plango.domain.auth.dto.PasswordResetRequestDto;
import dev.gmelon.plango.domain.auth.dto.SendEmailTokenRequestDto;
import dev.gmelon.plango.domain.auth.dto.SignupRequestDto;
import dev.gmelon.plango.domain.auth.dto.SnsLoginRequestDto;
import dev.gmelon.plango.domain.auth.dto.SnsRevokeRequestDto;
import dev.gmelon.plango.domain.auth.dto.TokenRefreshRequestDto;
import dev.gmelon.plango.domain.auth.dto.TokenResponseDto;
import dev.gmelon.plango.domain.auth.entity.EmailToken;
import dev.gmelon.plango.domain.auth.exception.EmailAuthenticationException;
import dev.gmelon.plango.domain.auth.exception.NoSuchRefreshTokenException;
import dev.gmelon.plango.domain.auth.exception.NotEmailMemberException;
import dev.gmelon.plango.domain.auth.exception.RefreshTokenTheftException;
import dev.gmelon.plango.domain.auth.repository.EmailTokenRepository;
import dev.gmelon.plango.domain.diary.repository.DiaryRepository;
import dev.gmelon.plango.domain.fcm.repository.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.entity.MemberType;
import dev.gmelon.plango.domain.member.exception.DuplicateEmailException;
import dev.gmelon.plango.domain.member.exception.DuplicateMemberException;
import dev.gmelon.plango.domain.member.exception.DuplicateNicknameException;
import dev.gmelon.plango.domain.member.exception.NoSuchMemberException;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.notification.repository.NotificationRepository;
import dev.gmelon.plango.domain.place.repository.PlaceSearchRecordRepository;
import dev.gmelon.plango.domain.refreshtoken.entity.RefreshToken;
import dev.gmelon.plango.domain.refreshtoken.repository.RefreshTokenRepository;
import dev.gmelon.plango.domain.schedule.repository.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import dev.gmelon.plango.domain.scheduleplace.repository.SchedulePlaceLikeRepository;
import dev.gmelon.plango.global.config.auth.jwt.JWTProvider;
import dev.gmelon.plango.global.config.auth.social.SocialClients;
import dev.gmelon.plango.global.config.auth.social.dto.SocialAccountResponse;
import dev.gmelon.plango.global.dto.StatusResponseDto;
import dev.gmelon.plango.global.infrastructure.mail.EmailSender;
import dev.gmelon.plango.global.infrastructure.mail.dto.EmailMessage;
import dev.gmelon.plango.global.infrastructure.s3.S3Repository;
import dev.gmelon.plango.global.util.RandomTokenGenerator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {
    private static final int RANDOM_PASSWORD_LENGTH = 9;
    private static final int EMAIL_TOKEN_LENGTH = 6;

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
    private final EmailTokenRepository emailTokenRepository;
    private final SocialClients socialClients;
    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;
    private final RandomTokenGenerator randomTokenGenerator;

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
    public void sendEmailToken(SendEmailTokenRequestDto requestDto) {
        validateEmailNotExists(requestDto.getEmail());

        String tokenValue = randomTokenGenerator.generate(EMAIL_TOKEN_LENGTH);
        EmailToken emailToken = EmailToken.builder()
                .email(requestDto.getEmail())
                .tokenValue(tokenValue)
                .build();
        emailTokenRepository.save(emailToken);

        sendEmailTokenMail(requestDto, tokenValue);
    }

    private void sendEmailTokenMail(SendEmailTokenRequestDto requestDto, String tokenValue) {
        String content = buildEmailTokenMailContent(tokenValue);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(requestDto.getEmail())
                .subject("[Plango] 메일 계정 인증 코드")
                .content(content)
                .build();
        emailSender.send(emailMessage);
    }

    private String buildEmailTokenMailContent(String tokenValue) {
        Context context = new Context();
        context.setVariable("tokenValue", tokenValue);

        return templateEngine.process("mail/emailToken", context);
    }

    @Transactional
    public StatusResponseDto checkEmailToken(CheckEmailTokenRequestDto requestDto) {
        Optional<EmailToken> optionalEmailToken = emailTokenRepository.findById(requestDto.getEmail());
        if (optionalEmailToken.isEmpty()) {
            logEmailTokenMismatch(requestDto.getEmail(), requestDto.getTokenValue());
            return StatusResponseDto.error();
        }

        EmailToken emailToken = optionalEmailToken.get();
        if (!emailToken.tokenValueEquals(requestDto.getTokenValue())) {
            logEmailTokenMismatch(requestDto.getEmail(), requestDto.getTokenValue());
            return StatusResponseDto.error();
        }

        emailToken.authenticate();
        emailTokenRepository.save(emailToken);
        return StatusResponseDto.ok();
    }

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        validateUniqueColumns(requestDto);
        validateEmailAuthenticated(requestDto);

        String encodePassword = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setEncodedPassword(encodePassword);
        memberRepository.save(requestDto.toEntity());
    }

    // TODO 리팩토링
    private void validateUniqueColumns(SignupRequestDto requestDto) {
        validateEmailNotExists(requestDto.getEmail());
        validateEmailNotExists(requestDto.getNickname());

        validateNicknameNotExists(requestDto.getEmail());
        validateNicknameNotExists(requestDto.getNickname());
    }

    private void validateEmailAuthenticated(SignupRequestDto requestDto) {
        Optional<EmailToken> optionalEmailToken = emailTokenRepository.findById(requestDto.getEmail());
        if (optionalEmailToken.isEmpty()) {
            logEmailTokenMismatch(requestDto.getEmail(), requestDto.getTokenValue());
            throw new EmailAuthenticationException();
        }

        EmailToken emailToken = optionalEmailToken.get();
        if (!(emailToken.isAuthenticated() && emailToken.tokenValueEquals(requestDto.getTokenValue()))) {
            logEmailTokenMismatch(requestDto.getEmail(), requestDto.getTokenValue());
            throw new EmailAuthenticationException();
        }
    }

    private void logEmailTokenMismatch(String email, String tokenValue) {
        log.warn("메일 인증 토큰 불일치. email: {}, tokenValue: {}", email, tokenValue);
    }

    private void validateEmailNotExists(String value) {
        if (isEmailAlreadyExists(value)) {
            throw new DuplicateEmailException();
        }
    }

    private void validateNicknameNotExists(String value) {
        if (isNicknameAlreadyExists(value)) {
            throw new DuplicateNicknameException();
        }
    }

    private boolean isEmailAlreadyExists(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    private boolean isNicknameAlreadyExists(String nickname) {
        return memberRepository.findByNickname(nickname).isPresent();
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

    @Transactional
    public void resetPassword(PasswordResetRequestDto requestDto) {
        Member member = findMemberByEmail(requestDto.getEmail());
        validateMemberType(member);

        String newRandomPassword = randomTokenGenerator.generate(RANDOM_PASSWORD_LENGTH);
        member.changePassword(passwordEncoder.encode(newRandomPassword));

        sendPasswordResetMail(member, newRandomPassword);
    }

    private void validateMemberType(Member member) {
        if (!member.typeEquals(MemberType.EMAIL)) {
            throw new NotEmailMemberException();
        }
    }

    private void sendPasswordResetMail(Member member, String newRandomPassword) {
        String content = buildPasswordResetMailContent(member, newRandomPassword);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(member.getEmail())
                .subject("[Plango] 비밀번호 재설정 안내")
                .content(content)
                .build();
        emailSender.send(emailMessage);
    }

    private String buildPasswordResetMailContent(Member member, String newRandomPassword) {
        Context context = new Context();
        context.setVariable("nickname", member.getNickname());
        context.setVariable("newPassword", newRandomPassword);

        return templateEngine.process("mail/passwordReset", context);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
