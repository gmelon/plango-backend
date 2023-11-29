package dev.gmelon.plango.service.member;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.exception.member.DuplicateNicknameException;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.member.PasswordMismatchException;
import dev.gmelon.plango.infrastructure.mail.EmailSender;
import dev.gmelon.plango.infrastructure.mail.dto.EmailMessage;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.member.dto.MemberEditProfileRequestDto;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberSearchRequestDto;
import dev.gmelon.plango.service.member.dto.MemberSearchResponseDto;
import dev.gmelon.plango.service.member.dto.PasswordChangeRequestDto;
import dev.gmelon.plango.service.member.dto.PasswordResetRequestDto;
import dev.gmelon.plango.service.member.dto.TermsAcceptedResponseDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private static final String WHITE_SPACE = " ";
    private static final String EMPTY_STRING = "";
    private static final List<Character> RANDOM_PASSWORD_CANDIDATES = new ArrayList<>();
    private static final int RANDOM_PASSWORD_LENGTH = 8;

    static {
        addRandomPasswordCandidates('a', 'z');
        addRandomPasswordCandidates('A', 'Z');
        addRandomPasswordCandidates('0', '9');
    }

    private static void addRandomPasswordCandidates(char startInclusive, char endInclusive) {
        for (char c = startInclusive; c <= endInclusive; c++) {
            RANDOM_PASSWORD_CANDIDATES.add(c);
        }
    }

    private final MemberRepository memberRepository;
    private final S3Repository s3Repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;

    public MemberProfileResponseDto getMyProfile(Long memberId) {
        return mapToProfileResponse(memberId);
    }

    public MemberProfileResponseDto getProfile(Long currentMemberId, Long targetMemberId) {

        // TODO 회원 차단 기능 구현 후 필터링 로직 추가

        return mapToProfileResponse(targetMemberId);
    }

    private MemberProfileResponseDto mapToProfileResponse(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberProfileResponseDto.from(member);
    }

    public List<MemberSearchResponseDto> searchWithoutCurrentMember(Long currentMemberId,
                                                                    MemberSearchRequestDto requestDto) {
        String trimmedNickname = trimWhiteSpaces(requestDto.getNickname());

        List<Member> members = memberRepository.searchByNicknameWithoutCurrentMember(currentMemberId, trimmedNickname);
        return members.stream()
                .map(MemberSearchResponseDto::from)
                .collect(toList());
    }

    private String trimWhiteSpaces(String nickname) {
        return nickname.replaceAll(WHITE_SPACE, EMPTY_STRING);
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
            throw new PasswordMismatchException();
        }
    }

    @Transactional
    public void editProfile(Long memberId, MemberEditProfileRequestDto requestDto) {
        Member member = findMemberById(memberId);
        validateNicknameIsUnique(requestDto, member.getNickname());

        String prevProfileImageUrl = member.getProfileImageUrl();
        member.edit(requestDto.toEditor());

        deletePrevImageIfChanged(prevProfileImageUrl, requestDto);
    }

    private void validateNicknameIsUnique(MemberEditProfileRequestDto requestDto, String currentMemberNickname) {
        if (requestDto.getNickname().equals(currentMemberNickname)) {
            return;
        }

        memberRepository.findByNickname(requestDto.getNickname())
                .ifPresent((member) -> {
                    throw new DuplicateNicknameException();
                });
    }

    private void deletePrevImageIfChanged(String prevProfileImageUrl, MemberEditProfileRequestDto requestDto) {
        if (prevProfileImageUrl == null) {
            return;
        }
        if (isImageChangedOrDeleted(prevProfileImageUrl, requestDto)) {
            s3Repository.delete(prevProfileImageUrl);
        }
    }

    private boolean isImageChangedOrDeleted(String prevProfileImageUrl, MemberEditProfileRequestDto requestDto) {
        return requestDto.getProfileImageUrl() == null || !prevProfileImageUrl.equals(requestDto.getProfileImageUrl());
    }

    public TermsAcceptedResponseDto termsAccepted(Long memberId) {
        Member member = findMemberById(memberId);
        return TermsAcceptedResponseDto.builder()
                .termsAccepted(member.isTermsAccepted())
                .build();
    }

    @Transactional
    public void acceptTerms(Long memberId) {
        Member member = findMemberById(memberId);
        member.acceptTerms();
    }

    @Transactional
    public void resetPassword(PasswordResetRequestDto requestDto) {
        Member member = findMemberByEmail(requestDto.getEmail());
        String newRandomPassword = createRandomPassword(RANDOM_PASSWORD_LENGTH);
        member.changePassword(passwordEncoder.encode(newRandomPassword));

        String content = buildPasswordResetMailContent(member, newRandomPassword);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(member.getEmail())
                .subject("[Plango] 비밀번호 재설정 안내")
                .content(content)
                .build();
        emailSender.send(emailMessage);
    }

    private String createRandomPassword(int length) {
        Collections.shuffle(RANDOM_PASSWORD_CANDIDATES);
        return RANDOM_PASSWORD_CANDIDATES.subList(0, length + 1).stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private String buildPasswordResetMailContent(Member member, String newRandomPassword) {
        Context context = new Context();
        context.setVariable("nickname", member.getNickname());
        context.setVariable("newPassword", newRandomPassword);

        return templateEngine.process("mail/passwordReset", context);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(NoSuchMemberException::new);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(NoSuchMemberException::new);
    }
}
