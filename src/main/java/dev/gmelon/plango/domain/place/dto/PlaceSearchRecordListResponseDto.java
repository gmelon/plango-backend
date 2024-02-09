package dev.gmelon.plango.domain.place.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.place.entity.PlaceSearchRecord;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PlaceSearchRecordListResponseDto {

    private Long id;

    private String keyword;

    private Double centerLatitude;

    private Double centerLongitude;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime lastSearchedDate;

    @Builder
    public PlaceSearchRecordListResponseDto(Long id, String keyword, Double centerLatitude, Double centerLongitude,
                                            LocalDateTime lastSearchedDate) {
        this.id = id;
        this.keyword = keyword;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.lastSearchedDate = lastSearchedDate;
    }

    public static PlaceSearchRecordListResponseDto from(PlaceSearchRecord entity) {
        return PlaceSearchRecordListResponseDto.builder()
                .id(entity.getId())
                .keyword(entity.getKeyword())
                .centerLatitude(entity.getCenterLatitude())
                .centerLongitude(entity.getCenterLongitude())
                .lastSearchedDate(entity.getLastSearchedDate())
                .build();
    }
}
