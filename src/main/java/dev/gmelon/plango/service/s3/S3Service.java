package dev.gmelon.plango.service.s3;

import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.s3.dto.FileDeleteRequestDto;
import dev.gmelon.plango.service.s3.dto.FileUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
