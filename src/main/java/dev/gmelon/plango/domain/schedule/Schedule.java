package dev.gmelon.plango.domain.schedule;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;

import dev.gmelon.plango.domain.BaseTimeEntity;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.schedule.place.SchedulePlace;
import dev.gmelon.plango.exception.schedule.ScheduleOwnerNotExistsException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Schedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(columnDefinition = "BOOLEAN DEFAULT 0", nullable = false)
    private boolean done;

    @OneToMany(mappedBy = "schedule", cascade = ALL, orphanRemoval = true)
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = ALL, orphanRemoval = true)
    private List<ScheduleMember> scheduleMembers = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = ALL, orphanRemoval = true)
    private List<SchedulePlace> schedulePlaces = new ArrayList<>();

    @Builder
    public Schedule(
            String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime,
            boolean done, List<Diary> diaries
    ) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.done = done;
        this.diaries = diaries;
    }

    public void edit(ScheduleEditor editor) {
        this.title = editor.getTitle();
        this.content = editor.getContent();
        this.date = editor.getDate();
        this.startTime = editor.getStartTime();
        this.endTime = editor.getEndTime();
    }

    public void setScheduleMembers(List<ScheduleMember> scheduleMembers) {
        this.scheduleMembers = scheduleMembers;
    }

    public void setSchedulePlaces(List<SchedulePlace> schedulePlaces) {
        this.schedulePlaces = schedulePlaces;
    }

    public void changeDone(boolean done) {
        this.done = done;
    }

    public void deleteScheduleMember(ScheduleMember scheduleMember) {
        scheduleMembers.remove(scheduleMember);
    }

    public void setSingleOwnerScheduleMember(Member member) {
        this.scheduleMembers = List.of(ScheduleMember.createOwner(member, this));
    }

    public void addSchedulePlace(SchedulePlace schedulePlace) {
        this.schedulePlaces.add(schedulePlace);
    }

    public void removeSchedulePlace(SchedulePlace schedulePlace) {
        this.schedulePlaces.remove(schedulePlace);
    }

    public boolean isMember(Long memberId) {
        return scheduleMembers.stream()
                .anyMatch(scheduleMember -> scheduleMember.isMemberEquals(memberId));
    }

    public boolean isAccepted(Long memberId) {
        return scheduleMembers.stream()
                .anyMatch(scheduleMember -> scheduleMember.isAccepted() && scheduleMember.isMemberEquals(memberId));
    }

    public boolean isOwner(Long memberId) {
        return scheduleMembers.stream()
                .anyMatch(scheduleMember -> scheduleMember.isOwner() && scheduleMember.isMemberEquals(memberId));
    }

    public Long ownerId() {
        return scheduleMembers.stream()
                .filter(ScheduleMember::isOwner)
                .map(ScheduleMember::memberId)
                .findAny()
                .orElseThrow(ScheduleOwnerNotExistsException::new);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Schedule schedule = (Schedule) o;
        return getId() != null && Objects.equals(getId(), schedule.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
