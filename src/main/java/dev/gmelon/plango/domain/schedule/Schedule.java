package dev.gmelon.plango.domain.schedule;

import dev.gmelon.plango.domain.BaseTimeEntity;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.exception.schedule.ScheduleOwnerNotExistsException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
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

    private Double latitude;

    private Double longitude;

    private String roadAddress;

    private String placeName;

    @Column(columnDefinition = "BOOLEAN DEFAULT 0", nullable = false)
    private boolean done;

    @OneToMany(mappedBy = "schedule", cascade = ALL, orphanRemoval = true)
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = ALL, orphanRemoval = true)
    private List<ScheduleMember> scheduleMembers = new ArrayList<>();

    @Builder
    public Schedule(
            String title, String content, LocalDate date, LocalTime startTime, LocalTime endTime,
            Double latitude, Double longitude, String roadAddress, String placeName, boolean done,
            List<Diary> diaries
    ) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.placeName = placeName;
        this.done = done;
        this.diaries = diaries;
    }

    public void edit(ScheduleEditor editor) {
        this.title = editor.getTitle();
        this.content = editor.getContent();
        this.date = editor.getDate();
        this.startTime = editor.getStartTime();
        this.endTime = editor.getEndTime();
        this.latitude = editor.getLatitude();
        this.longitude = editor.getLongitude();
        this.roadAddress = editor.getRoadAddress();
        this.placeName = editor.getPlaceName();
    }

    public void changeDone(boolean done) {
        this.done = done;
    }

    public void setScheduleMembers(List<ScheduleMember> scheduleMembers) {
        this.scheduleMembers = scheduleMembers;
    }

    public void setSingleOwnerScheduleMember(Member member) {
        this.scheduleMembers = List.of(ScheduleMember.createOwner(member, this));
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
