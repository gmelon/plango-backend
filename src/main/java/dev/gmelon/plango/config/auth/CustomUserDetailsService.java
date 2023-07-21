package dev.gmelon.plango.config.auth;

import dev.gmelon.plango.config.auth.dto.MemberPrincipal;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
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
        // TODO 예외 어떻게 처리할지? 현재는 이메일/닉네임이 잘못되면 예외 발생
        return memberRepository.findByEmail(emailOrNickname)
                .orElseGet(() -> memberRepository.findByNickname(emailOrNickname)
                        .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.")));
    }

}
