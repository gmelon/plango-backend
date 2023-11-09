package dev.gmelon.plango.config.auth.dto;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRole;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class MemberPrincipal extends User {
    private static final int FIRST_MATCHED_ROLE_INDEX = 0;
    private static final String EMPTY_CREDENTIALS = "";

    @Getter
    private Long id;

    public MemberPrincipal(Member member) {
        super(member.getEmail(), member.getPassword(), Collections.singletonList(new SimpleGrantedAuthority(member.getRole())));
        this.id = member.getId();
    }

    public static MemberPrincipal of(String email, List<? extends GrantedAuthority> authorities) {
        Member member = Member.builder()
                .email(email)
                .password(EMPTY_CREDENTIALS)
                .role(MemberRole.parse(authorities.get(FIRST_MATCHED_ROLE_INDEX).toString()))
                .build();
        return new MemberPrincipal(member);
    }
}
