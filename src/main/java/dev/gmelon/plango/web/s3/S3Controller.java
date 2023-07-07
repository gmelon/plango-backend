package dev.gmelon.plango.web.s3;

import dev.gmelon.plango.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Validated // TODO validated가 더 깔끔한 응답을 내려주는 것 같음
@RequestMapping("/api/v1/s3")
@RestController
public class S3Controller {

    private final URLValidator urlValidator = new URLValidator();
    private final S3Service s3Service;

    @PostMapping
    public String upload(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일입니다.");
        }
        return s3Service.upload(file);
    }

    @DeleteMapping
    public void delete(@RequestParam @URL String savedFileUrl) {
        s3Service.delete(savedFileUrl);
    }

}
