package dev.gmelon.plango.domain.fcm;

import dev.gmelon.plango.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
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
        if (lastUpdatedDate == null) {
            this.lastUpdatedDate = LocalDateTime.now();
        }
    }

    public boolean isMemberEquals(Long memberId) {
        return member.getId().equals(memberId);
    }

    public void update() {
        this.lastUpdatedDate = LocalDateTime.now();
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
