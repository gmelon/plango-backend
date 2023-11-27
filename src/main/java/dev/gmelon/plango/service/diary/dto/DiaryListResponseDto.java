package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.Diary;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class DiaryListResponseDto {
    private Long id;
    private String firstImageUrl;
    private int imageCount;
    private String content;

    @Builder
    public DiaryListResponseDto(Long id, String firstImageUrl, int imageCount, String content) {
        this.id = id;
        this.firstImageUrl = firstImageUrl;
        this.imageCount = imageCount;
        this.content = content;
    }

    public static DiaryListResponseDto from(Diary diary) {
        return DiaryListResponseDto.builder()
                .id(diary.getId())
                .firstImageUrl(firstImageUrl(diary))
                .imageCount(diary.getDiaryImageUrls().size())
                .content(diary.getContent())
                .build();
    }

    private static String firstImageUrl(Diary diary) {
        if (diary.hasImage()) {
            return diary.getDiaryImageUrls().get(0);
        }
        return null;
    }
}
