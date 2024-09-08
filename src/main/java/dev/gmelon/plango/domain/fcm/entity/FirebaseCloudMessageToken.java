package dev.gmelon.plango.domain.fcm.entity;

import static javax.persistence.GenerationType.IDENTITY;

import dev.gmelon.plango.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FirebaseCloudMessageToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(unique = true, nullable = false)
    private String tokenValue;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedDate;

    @Builder
    public FirebaseCloudMessageToken(Long id, Member member, String tokenValue, LocalDateTime lastUpdatedDate) {
        this.id = id;
        this.member = member;
        this.tokenValue = tokenValue;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public boolean isMemberEquals(Long memberId) {
        return member.getId().equals(memberId);
    }

    public void update(LocalDateTime currentDate) {
        this.lastUpdatedDate = currentDate;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        FirebaseCloudMessageToken that = (FirebaseCloudMessageToken) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
