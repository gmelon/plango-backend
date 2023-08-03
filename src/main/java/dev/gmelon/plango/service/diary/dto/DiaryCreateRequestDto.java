package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.Diary;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@NoArgsConstructor
@Getter
public class DiaryCreateRequestDto {

    private String content;

    @URL
    private String imageUrl;

    @Builder
    public DiaryCreateRequestDto(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public Diary toEntity() {
        return Diary.builder()
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

}
