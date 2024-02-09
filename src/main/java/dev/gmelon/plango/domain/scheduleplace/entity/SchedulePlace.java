package dev.gmelon.plango.domain.scheduleplace.entity;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.global.entity.BaseTimeEntity;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class SchedulePlace extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String placeName;

    private String roadAddress;

    private Double latitude;

    private Double longitude;

    private String memo;

    @Column(length = 10)
    private String category;

    @Column(columnDefinition = "BOOLEAN DEFAULT 0", nullable = false)
    private boolean confirmed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @OneToMany(mappedBy = "schedulePlace", cascade = ALL, orphanRemoval = true)
    private Set<SchedulePlaceLike> schedulePlaceLikes = new HashSet<>();

    @Builder
    public SchedulePlace(Long id, String placeName, String roadAddress, Double latitude, Double longitude, String memo,
                         String category, boolean confirmed, Schedule schedule) {
        this.id = id;
        this.placeName = placeName;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.memo = memo;
        this.category = category;
        this.confirmed = confirmed;
        this.schedule = schedule;
    }

    public void edit(SchedulePlaceEditor editor) {
        this.memo = editor.getMemo();
        this.category = editor.getCategory();
    }

    public void confirm() {
        confirmed = true;
    }

    public void deny() {
        confirmed = false;
    }

    // TODO 동시성 문제 해결
    public void like(Member member) {
        SchedulePlaceLike like = SchedulePlaceLike.builder()
                .member(member)
                .schedulePlace(this)
                .build();
        this.schedulePlaceLikes.add(like);
    }

    public void dislike(Long memberId) {
        schedulePlaceLikes.removeIf(like -> like.isMemberAndPlaceEquals(memberId, this.id));
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SchedulePlace that = (SchedulePlace) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
