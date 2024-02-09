package dev.gmelon.plango.domain.diary.dto;

import dev.gmelon.plango.domain.diary.entity.Diary;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.global.web.validator.CollectionURLValidation;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
