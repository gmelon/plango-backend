package dev.gmelon.plango.service.s3;

import dev.gmelon.plango.infrastructure.s3.S3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class S3Service {

    private final S3Repository s3Repository;

    public String upload(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return s3Repository.upload(Objects.requireNonNull(file.getOriginalFilename()), inputStream, file.getContentType(), file.getSize());
        } catch (IOException e) {
            // TODO 사용자 예외 UploadFailException? (500)
            throw new RuntimeException("파일 업로드에 실패했습니다.");
        }
    }

    public void delete(String savedFileUrl) {
        s3Repository.delete(savedFileUrl);
    }
}
