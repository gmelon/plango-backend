package dev.gmelon.plango.domain.diary.dto;

import dev.gmelon.plango.domain.diary.entity.DiaryEditor;
import dev.gmelon.plango.global.web.validator.CollectionURLValidation;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class DiaryEditRequestDto {

    private String content;

    @CollectionURLValidation
    private List<String> imageUrls = new ArrayList<>();

    @Builder
    public DiaryEditRequestDto(String content, List<String> imageUrls) {
        this.content = content;
        this.imageUrls = imageUrls;
    }

    public DiaryEditor toEditor() {
        return DiaryEditor.builder()
                .content(content)
                .imageUrls(imageUrls)
                .build();
    }

}
