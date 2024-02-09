package dev.gmelon.plango.domain.member.entity;

import static javax.persistence.GenerationType.IDENTITY;

import dev.gmelon.plango.global.entity.BaseTimeEntity;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(length = 50)
    private String bio;

    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter(AccessLevel.NONE)
    private MemberRole role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberType type;

    @Column(columnDefinition = "BOOLEAN DEFAULT 0", nullable = false)
    private boolean termsAccepted;

    @Builder
    public Member(Long id, String email, String password, String nickname, String bio, String profileImageUrl,
                  MemberRole role, MemberType type, boolean termsAccepted) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.type = type;
        this.termsAccepted = termsAccepted;
    }

    public String getRole() {
        return role.toString();
    }

    public void changeRole(MemberRole role) {
        this.role = role;
    }

    public void edit(MemberEditor editor) {
        this.nickname = editor.getNickname();
        this.profileImageUrl = editor.getProfileImageUrl();
        this.bio = editor.getBio();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void acceptTerms() {
        this.termsAccepted = true;
    }

    public void rejectTerms() {
        this.termsAccepted = false;
    }

    public boolean typeEquals(MemberType type) {
        return this.type == type;
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
