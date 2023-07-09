package dev.gmelon.plango.config.auth.dto;

import dev.gmelon.plango.domain.member.Member;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

public class MemberPrincipal extends User {

    @Getter
    private Long id;

    public MemberPrincipal(Member member) {
        super(member.getEmail(), member.getPassword(), Collections.singletonList(new SimpleGrantedAuthority(member.getRole().toString())));
        this.id = member.getId();
    }

}
