package dev.gmelon.plango.domain.diary;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class DiaryEditor {

    private String content;
    private List<String> imageUrls;

    @Builder
    public DiaryEditor(String content, List<String> imageUrls) {
        this.content = content;
        this.imageUrls = imageUrls;
    }
}
