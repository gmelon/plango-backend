package dev.gmelon.plango.config.security;

import dev.gmelon.plango.config.auth.dto.MemberPrincipal;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class PlangoMockSecurityContext implements WithSecurityContextFactory<PlangoMockUser> {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public SecurityContext createSecurityContext(PlangoMockUser annotation) {
        MemberPrincipal principal = createMemberPrincipal(annotation);
        return createSecurityContext(principal);
    }

    private MemberPrincipal createMemberPrincipal(PlangoMockUser annotation) {
        signupMember(annotation);
        Member member = memberRepository.findAll().get(0);
        return new MemberPrincipal(member);
    }

    private void signupMember(PlangoMockUser annotation) {
        Member member = Member.builder()
                .email(annotation.email())
                .password(passwordEncoder.encode(annotation.password()))
                .nickname(annotation.nickname())
                .role(annotation.role())
                .type(annotation.type())
                .termsAccepted(annotation.termsAccepted())
                .build();

        memberRepository.save(member);
    }

    private SecurityContext createSecurityContext(MemberPrincipal principal) {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                principal.getPassword(),
                principal.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);

        return context;
    }
}
