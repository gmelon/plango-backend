package dev.gmelon.plango.domain.diary.entity;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

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
