package dev.gmelon.plango.domain.schedule.place;

import dev.gmelon.plango.domain.BaseTimeEntity;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.schedule.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
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
        this.placeName = editor.getPlaceName();
        this.roadAddress = editor.getRoadAddress();
        this.latitude = editor.getLatitude();
        this.longitude = editor.getLongitude();
        this.memo = editor.getMemo();
        this.category = editor.getCategory();
    }

    public List<Long> likedMemberIds() {
        return schedulePlaceLikes.stream()
                .map(SchedulePlaceLike::memberId)
                .collect(toList());
    }

    public boolean isMemberLikes(Long memberId) {
        return schedulePlaceLikes.stream()
                .anyMatch(like -> like.isMemberEquals(memberId));
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
