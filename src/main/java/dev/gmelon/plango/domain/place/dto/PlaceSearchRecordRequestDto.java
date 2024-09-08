package dev.gmelon.plango.domain.place.dto;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.place.entity.PlaceSearchRecord;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PlaceSearchRecordRequestDto {

    @NotBlank
    private String keyword;

    private Double centerLatitude;

    private Double centerLongitude;

    @Builder
    public PlaceSearchRecordRequestDto(String keyword, Double centerLatitude, Double centerLongitude) {
        this.keyword = keyword;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
    }

    public PlaceSearchRecord toEntity(Member member, LocalDateTime currentDate) {
        return PlaceSearchRecord.builder()
                .keyword(keyword)
                .centerLatitude(centerLatitude)
                .centerLongitude(centerLongitude)
                .member(member)
                .lastSearchedDate(currentDate)
                .build();
    }

}
