package dev.gmelon.plango.domain.schedule;

import dev.gmelon.plango.domain.BaseTimeEntity;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
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

    @Lob
    private String content;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private String location;

    @Column(columnDefinition = "BOOLEAN DEFAULT 'false'", nullable = false)
    private boolean done;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Builder
    public Schedule(String title, String content, LocalDateTime startTime, LocalDateTime endTime, String location, boolean done, Member member, Diary diary) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.done = done;
        this.member = member;
        this.diary = diary;
    }

    public void edit(ScheduleEditor editor) {
        this.title = editor.getTitle();
        this.content = editor.getContent();
        this.startTime = editor.getStartTime();
        this.endTime = editor.getEndTime();
        this.location = editor.getLocation();
    }

    public void addDiary(Diary diary) {
        this.diary = diary;
    }

    public void deleteDiary() {
        this.diary = null;
    }

    public void changeDone(boolean done) {
        this.done = done;
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
