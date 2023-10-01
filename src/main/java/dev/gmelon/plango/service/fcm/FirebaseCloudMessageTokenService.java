package dev.gmelon.plango.service.fcm;

import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageToken;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.exception.fcm.FirebaseCloudMessageTokenAccessDeniedException;
import dev.gmelon.plango.exception.fcm.NoSuchFirebaseCloudMessageTokenException;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.service.fcm.dto.FirebaseCloudMessageTokenCreateOrUpdateRequestDto;
import dev.gmelon.plango.service.fcm.dto.FirebaseCloudMessageTokenDeleteRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FirebaseCloudMessageTokenService {

    private final FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;
    private final MemberRepository memberRepository;

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
                .build();
        firebaseCloudMessageTokenRepository.save(token);
    }

    private void update(Optional<FirebaseCloudMessageToken> tokenOptional) {
        FirebaseCloudMessageToken token = tokenOptional.get();
        token.update();
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
