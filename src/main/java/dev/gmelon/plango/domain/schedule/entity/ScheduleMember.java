package dev.gmelon.plango.domain.schedule.entity;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import dev.gmelon.plango.domain.member.entity.Member;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
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
public class ScheduleMember {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column(columnDefinition = "BOOLEAN DEFAULT 0", nullable = false)
    private boolean owner;

    @Column(columnDefinition = "BOOLEAN DEFAULT 0", nullable = false)
    private boolean accepted;

    @Builder
    public ScheduleMember(Member member, Schedule schedule, boolean owner, boolean accepted) {
        this.member = member;
        this.schedule = schedule;
        this.owner = owner;
        this.accepted = accepted;
    }

    public static ScheduleMember createOwner(Member member, Schedule schedule) {
        return ScheduleMember.builder()
                .owner(true)
                .accepted(true)
                .member(member)
                .schedule(schedule)
                .build();
    }

    public static ScheduleMember createParticipant(Member member, Schedule schedule) {
        return ScheduleMember.builder()
                .owner(false)
                .accepted(false)
                .member(member)
                .schedule(schedule)
                .build();
    }

    public void accept() {
        this.accepted = true;
    }

    public Long memberId() {
        return member.getId();
    }

    public boolean isMemberEquals(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ScheduleMember that = (ScheduleMember) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
