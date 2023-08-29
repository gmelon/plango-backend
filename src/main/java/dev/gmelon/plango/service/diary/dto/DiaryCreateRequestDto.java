package dev.gmelon.plango.service.diary.dto;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.util.validator.CollectionURLValidation;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class DiaryCreateRequestDto {

    private String content;

    @CollectionURLValidation
    private List<String> imageUrls = new ArrayList<>();

    @Builder
    public DiaryCreateRequestDto(String content, List<String> imageUrls) {
        this.content = content;
        this.imageUrls = imageUrls;
    }

    public Diary toEntity(Member member, Schedule schedule) {
        return Diary.builder()
                .content(content)
                .imageUrls(imageUrls)
                .member(member)
                .schedule(schedule)
                .build();
    }

}
