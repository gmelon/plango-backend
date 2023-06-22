package dev.gmelon.plango.domain.schedule;

import dev.gmelon.plango.domain.BaseTimeEntity;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Builder
    public Schedule(String title, String content, LocalDateTime startTime, LocalDateTime endTime, Member member, Diary diary) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.member = member;
        this.diary = diary;
    }
}
