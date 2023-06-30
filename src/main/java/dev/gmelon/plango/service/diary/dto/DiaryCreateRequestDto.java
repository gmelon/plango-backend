package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.Diary;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class DiaryCreateRequestDto {

    @NotBlank
    private String title;

    private String content;

    @URL
    private String imageUrl;

    @Builder
    public DiaryCreateRequestDto(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public Diary toEntity() {
        return Diary.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

}
