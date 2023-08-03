package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.DiaryEditor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@NoArgsConstructor
@Getter
public class DiaryEditRequestDto {

    private String content;

    @URL
    private String imageUrl;

    @Builder
    public DiaryEditRequestDto(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public DiaryEditor toEditor() {
        return DiaryEditor.builder()
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

}
