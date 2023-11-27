package dev.gmelon.plango.domain.diary;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import dev.gmelon.plango.domain.BaseTimeEntity;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.schedule.Schedule;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
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
public class Diary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "diary", cascade = ALL, orphanRemoval = true)
    private List<DiaryImage> diaryImages = new ArrayList<>();

    @Builder
    public Diary(String content, Schedule schedule, Member member, List<String> imageUrls) {
        this.content = content;
        this.schedule = schedule;
        this.member = member;
        setDiaryImages(imageUrls);
    }

    public Long memberId() {
        return member.getId();
    }

    public void edit(DiaryEditor editor) {
        this.content = editor.getContent();
        setDiaryImages(editor.getImageUrls());
    }

    private void setDiaryImages(List<String> imageUrls) {
        diaryImages.clear();

        if (imageUrls == null) {
            return;
        }

        diaryImages.addAll(mapToDiaryImages(imageUrls));
    }

    public boolean hasImage() {
        return !diaryImages.isEmpty();
    }

    private List<DiaryImage> mapToDiaryImages(List<String> imageUrls) {
        return imageUrls.stream()
                .map(imageUrl -> DiaryImage.builder()
                        .diary(this)
                        .imageUrl(imageUrl)
                        .build())
                .collect(Collectors.toList());
    }

    public List<String> getDiaryImageUrls() {
        return diaryImages.stream()
                .map(DiaryImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Diary diary = (Diary) o;
        return getId() != null && Objects.equals(getId(), diary.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
