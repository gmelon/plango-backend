package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.DiaryEditor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class DiaryEditRequestDto {

    @NotBlank
    private String title;

    private String content;

    @URL
    private String imageUrl;

    @Builder
    public DiaryEditRequestDto(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public DiaryEditor toDiaryEditor() {
        return DiaryEditor.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

}