package dev.gmelon.plango.domain.s3.service;

import dev.gmelon.plango.domain.s3.dto.FileDeleteRequestDto;
import dev.gmelon.plango.domain.s3.dto.FileUploadResponseDto;
import dev.gmelon.plango.global.infrastructure.s3.S3Repository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class S3Service {

    private final S3Repository s3Repository;

    public FileUploadResponseDto uploadAll(List<MultipartFile> files) {
        List<String> uploadedFileUrls = s3Repository.uploadAll(files);
        return new FileUploadResponseDto(uploadedFileUrls);
    }

    public void deleteAll(FileDeleteRequestDto requestDto) {
        s3Repository.deleteAll(requestDto.getSavedFileUrls());
    }
}
