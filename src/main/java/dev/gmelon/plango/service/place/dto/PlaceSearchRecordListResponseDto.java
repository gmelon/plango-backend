package dev.gmelon.plango.service.place.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.gmelon.plango.domain.place.PlaceSearchRecord;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class PlaceSearchRecordListResponseDto {

    private Long id;

    private String keyword;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime lastSearchedDate;

    @Builder
    public PlaceSearchRecordListResponseDto(Long id, String keyword, LocalDateTime lastSearchedDate) {
        this.id = id;
        this.keyword = keyword;
        this.lastSearchedDate = lastSearchedDate;
    }

    public static PlaceSearchRecordListResponseDto from(PlaceSearchRecord entity) {
        return PlaceSearchRecordListResponseDto.builder()
                .id(entity.getId())
                .keyword(entity.getKeyword())
                .lastSearchedDate(entity.getLastSearchedDate())
                .build();
    }
}
