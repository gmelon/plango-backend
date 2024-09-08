package dev.gmelon.plango.domain.s3.dto;

import dev.gmelon.plango.global.web.validator.CollectionURLValidation;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FileDeleteRequestDto {

    @CollectionURLValidation
    List<String> savedFileUrls = new ArrayList<>();

    public FileDeleteRequestDto(List<String> savedFileUrls) {
        this.savedFileUrls = savedFileUrls;
    }
}
