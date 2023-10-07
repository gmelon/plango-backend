package dev.gmelon.plango.domain.schedule.place;

import dev.gmelon.plango.domain.BaseTimeEntity;
import dev.gmelon.plango.domain.member.Member;
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
public class SchedulePlaceLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_place_id")
    private SchedulePlace schedulePlace;

    @Builder
    public SchedulePlaceLike(Long id, Member member, SchedulePlace schedulePlace) {
        this.id = id;
        this.member = member;
        this.schedulePlace = schedulePlace;
    }

    public boolean isMemberEquals(Long memberId) {
        return Objects.equals(memberId(), memberId);
    }

    public boolean isMemberAndPlaceEquals(Long memberId, Long schedulePlaceId) {
        return Objects.equals(memberId(), memberId) && Objects.equals(schedulePlaceId(), schedulePlaceId);
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
        SchedulePlaceLike that = (SchedulePlaceLike) o;
        return isMemberAndPlaceEquals(that.memberId(), that.schedulePlaceId());
    }

    private Long schedulePlaceId() {
        return schedulePlace.getId();
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
