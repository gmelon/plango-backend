package dev.gmelon.plango.service.s3.dto;

import dev.gmelon.plango.util.validator.CollectionURLValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class FileDeleteRequestDto {

    @CollectionURLValidation
    List<String> savedFileUrls = new ArrayList<>();

    public FileDeleteRequestDto(List<String> savedFileUrls) {
        this.savedFileUrls = savedFileUrls;
    }
}
