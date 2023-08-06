package dev.gmelon.plango.service.s3.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class FileUploadResponseDto {

    private List<String> uploadedFileUrls;

    public FileUploadResponseDto(List<String> uploadedFileUrls) {
        this.uploadedFileUrls = uploadedFileUrls;
    }

}
