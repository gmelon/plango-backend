package dev.gmelon.plango.web.s3;

import dev.gmelon.plango.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api/v1/s3")
@RestController
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping
    public String upload(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일입니다.");
        }
        return s3Service.upload(file);
    }

}
