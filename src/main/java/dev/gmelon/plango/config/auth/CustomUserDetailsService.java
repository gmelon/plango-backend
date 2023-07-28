package dev.gmelon.plango.config.auth;

import dev.gmelon.plango.config.auth.dto.MemberPrincipal;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.exception.ErrorMessages;
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
