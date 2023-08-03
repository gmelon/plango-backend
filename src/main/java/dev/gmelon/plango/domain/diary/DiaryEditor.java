package dev.gmelon.plango.domain.diary;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DiaryEditor {

    private String content;
    private String imageUrl;

    @Builder
    public DiaryEditor(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }
}
