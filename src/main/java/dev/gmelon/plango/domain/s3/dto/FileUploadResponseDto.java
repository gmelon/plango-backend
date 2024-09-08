package dev.gmelon.plango.domain.s3.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FileUploadResponseDto {

    private List<String> uploadedFileUrls;

    public FileUploadResponseDto(List<String> uploadedFileUrls) {
        this.uploadedFileUrls = uploadedFileUrls;
    }

}
