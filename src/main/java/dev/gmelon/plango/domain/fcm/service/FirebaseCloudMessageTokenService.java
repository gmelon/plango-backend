package dev.gmelon.plango.domain.fcm.service;

import dev.gmelon.plango.domain.fcm.dto.FirebaseCloudMessageTokenCreateOrUpdateRequestDto;
import dev.gmelon.plango.domain.fcm.dto.FirebaseCloudMessageTokenDeleteRequestDto;
import dev.gmelon.plango.domain.fcm.entity.FirebaseCloudMessageToken;
import dev.gmelon.plango.domain.fcm.exception.FirebaseCloudMessageTokenAccessDeniedException;
import dev.gmelon.plango.domain.fcm.exception.NoSuchFirebaseCloudMessageTokenException;
import dev.gmelon.plango.domain.fcm.repository.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.exception.NoSuchMemberException;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FirebaseCloudMessageTokenService {

    private final FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Transactional
    public void createOrUpdate(Long memberId, FirebaseCloudMessageTokenCreateOrUpdateRequestDto requestDto) {
        Optional<FirebaseCloudMessageToken> tokenOptional = firebaseCloudMessageTokenRepository.findByTokenValue(requestDto.getTokenValue());

        if (tokenOptional.isPresent()) {
            update(tokenOptional);
            return;
        }

        create(memberId, requestDto);
    }

    private void create(Long memberId, FirebaseCloudMessageTokenCreateOrUpdateRequestDto requestDto) {
        FirebaseCloudMessageToken token = FirebaseCloudMessageToken.builder()
                .member(findMemberById(memberId))
                .tokenValue(requestDto.getTokenValue())
                .lastUpdatedDate(LocalDateTime.now(clock))
                .build();
        firebaseCloudMessageTokenRepository.save(token);
    }

    private void update(Optional<FirebaseCloudMessageToken> tokenOptional) {
        FirebaseCloudMessageToken token = tokenOptional.get();
        token.update(LocalDateTime.now(clock));
    }

    @Transactional
    public void delete(Long memberId, FirebaseCloudMessageTokenDeleteRequestDto requestDto) {
        FirebaseCloudMessageToken token = findTokenByTokenValue(requestDto.getTokenValue());
        validateTokenOwner(memberId, token);

        firebaseCloudMessageTokenRepository.delete(token);
    }

    private void validateTokenOwner(Long memberId, FirebaseCloudMessageToken token) {
        if (!token.isMemberEquals(memberId)) {
            throw new FirebaseCloudMessageTokenAccessDeniedException();
        }
    }

    private FirebaseCloudMessageToken findTokenByTokenValue(String tokenValue) {
        return firebaseCloudMessageTokenRepository.findByTokenValue(tokenValue)
                .orElseThrow(NoSuchFirebaseCloudMessageTokenException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }

}
