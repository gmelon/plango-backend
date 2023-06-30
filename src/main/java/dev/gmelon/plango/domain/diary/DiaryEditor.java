package dev.gmelon.plango.domain.diary;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DiaryEditor {

    private String title;
    private String content;
    private String imageUrl;

    @Builder
    public DiaryEditor(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }
}
