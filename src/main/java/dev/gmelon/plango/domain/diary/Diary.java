package dev.gmelon.plango.domain.diary;

import dev.gmelon.plango.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Diary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    private String imageUrl;

    @Builder
    public Diary(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

}
