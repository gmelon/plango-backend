package dev.gmelon.plango.domain.diary.dto;

import dev.gmelon.plango.domain.diary.entity.Diary;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class DiarySearchResponseDto {

    private Long id;

    private String content;

    private String scheduleTitle;

    @Builder
    public DiarySearchResponseDto(Long id, String content, String scheduleTitle) {
        this.id = id;
        this.content = content;
        this.scheduleTitle = scheduleTitle;
    }

    public static DiarySearchResponseDto from(Diary diary) {
        return DiarySearchResponseDto.builder()
                .id(diary.getId())
                .content(diary.getContent())
                .scheduleTitle(diary.getSchedule().getTitle())
                .build();
    }
}
