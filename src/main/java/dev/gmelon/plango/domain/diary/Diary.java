package dev.gmelon.plango.domain.diary;

import dev.gmelon.plango.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Diary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "diary", cascade = ALL, orphanRemoval = true)
    private List<DiaryImage> diaryImages = new ArrayList<>();

    @Builder
    public Diary(String content, List<String> imageUrls) {
        this.content = content;
        setDiaryImages(imageUrls);
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
