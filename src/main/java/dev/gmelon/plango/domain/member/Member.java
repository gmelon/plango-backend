package dev.gmelon.plango.domain.member;

import dev.gmelon.plango.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Builder
    public Member(String email, String password, String nickname, String profileImageUrl, MemberRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
    }

    public void edit(MemberEditor editor) {
        this.nickname = editor.getNickname();
        this.profileImageUrl = editor.getProfileImageUrl();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Member member = (Member) o;
        return getId() != null && Objects.equals(getId(), member.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
