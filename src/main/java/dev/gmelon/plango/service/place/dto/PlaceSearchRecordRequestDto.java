package dev.gmelon.plango.service.place.dto;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.place.PlaceSearchRecord;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class PlaceSearchRecordRequestDto {

    @NotBlank
    private String keyword;

    @Builder
    public PlaceSearchRecordRequestDto(String keyword) {
        this.keyword = keyword;
    }

    public PlaceSearchRecord toEntity(Member member, LocalDateTime currentDate) {
        return PlaceSearchRecord.builder()
                .keyword(keyword)
                .member(member)
                .lastSearchedDate(currentDate)
                .build();
    }

}
