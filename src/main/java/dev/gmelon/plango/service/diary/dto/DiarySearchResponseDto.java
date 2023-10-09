package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.Diary;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class DiarySearchResponseDto {

    private Long id;

    private String content;

    @Builder
    public DiarySearchResponseDto(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    public static DiarySearchResponseDto from(Diary diary) {
        return DiarySearchResponseDto.builder()
                .id(diary.getId())
                .content(diary.getContent())
                .build();
    }
}
