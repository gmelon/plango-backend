package dev.gmelon.plango.domain.place;

import dev.gmelon.plango.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class PlaceSearchRecord {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private LocalDateTime lastSearchedDate;

    @Column(nullable = false)
    private String keyword;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public PlaceSearchRecord(LocalDateTime lastSearchedDate, String keyword, Member member) {
        this.lastSearchedDate = lastSearchedDate;
        this.keyword = keyword;
        this.member = member;

        setDefaultValuesWhenNull();
    }

    private void setDefaultValuesWhenNull() {
        if (this.lastSearchedDate == null) {
            this.lastSearchedDate = LocalDateTime.now();
        }
    }

    public void search() {
        this.lastSearchedDate = LocalDateTime.now();
    }

    public Long memberId() {
        return member.getId();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        PlaceSearchRecord that = (PlaceSearchRecord) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}