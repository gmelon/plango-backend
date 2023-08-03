package dev.gmelon.plango.config.security;

import dev.gmelon.plango.config.auth.dto.MemberPrincipal;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.service.auth.AuthService;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class PlangoMockSecurityContext implements WithSecurityContextFactory<PlangoMockUser> {

    private final MemberRepository memberRepository;
    private final AuthService authService;

    @Transactional
    @Override
    public SecurityContext createSecurityContext(PlangoMockUser annotation) {
        MemberPrincipal principal = createMemberPrincipal(annotation);
        return createSecurityContext(principal);
    }

    private MemberPrincipal createMemberPrincipal(PlangoMockUser annotation) {
        signupMember(annotation);

        Member member = memberRepository.findAll().get(0);
        changeMemberRoleIfNotUser(annotation, member);

        return new MemberPrincipal(member);
    }

    private void changeMemberRoleIfNotUser(PlangoMockUser annotation, Member member) {
        if (annotation.role() !=  MemberRole.ROLE_USER) {
            member.changeRole(annotation.role());
        }
    }

    private void signupMember(PlangoMockUser annotation) {
        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                .email(annotation.email())
                .password(annotation.password())
                .nickname(annotation.nickname())
                .build();
        authService.signup(signupRequestDto);
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
