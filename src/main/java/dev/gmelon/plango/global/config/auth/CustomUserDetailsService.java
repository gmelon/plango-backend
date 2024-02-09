package dev.gmelon.plango.global.config.auth;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.global.config.auth.dto.MemberPrincipal;
import dev.gmelon.plango.global.exception.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Member member = findMemberByEmailOrNickname(emailOrNickname);
        return new MemberPrincipal(member);
    }

    private Member findMemberByEmailOrNickname(String emailOrNickname) {
        return memberRepository.findByEmail(emailOrNickname)
                .orElseGet(() -> memberRepository.findByNickname(emailOrNickname)
                        .orElseThrow(() -> new UsernameNotFoundException(ErrorMessages.LOGIN_FAILURE_ERROR_MESSAGE)));
    }

}
