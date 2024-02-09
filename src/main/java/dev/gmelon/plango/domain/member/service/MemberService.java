package dev.gmelon.plango.domain.member.service;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.member.dto.MemberEditProfileRequestDto;
import dev.gmelon.plango.domain.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.domain.member.dto.MemberSearchRequestDto;
import dev.gmelon.plango.domain.member.dto.MemberSearchResponseDto;
import dev.gmelon.plango.domain.member.dto.PasswordChangeRequestDto;
import dev.gmelon.plango.domain.member.dto.TermsAcceptedResponseDto;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.exception.DuplicateNicknameException;
import dev.gmelon.plango.domain.member.exception.NoSuchMemberException;
import dev.gmelon.plango.domain.member.exception.PasswordMismatchException;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.global.infrastructure.s3.S3Repository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private static final String WHITE_SPACE = " ";
    private static final String EMPTY_STRING = "";

    private final MemberRepository memberRepository;
    private final S3Repository s3Repository;
    private final PasswordEncoder passwordEncoder;

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

        memberRepository.findByEmail(requestDto.getNickname())
                .ifPresent((member) -> {
                    throw new DuplicateNicknameException();
                });
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

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(NoSuchMemberException::new);
    }
}
