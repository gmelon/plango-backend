package dev.gmelon.plango.web.s3;

import dev.gmelon.plango.exception.s3.EmptyFileException;
import dev.gmelon.plango.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Validated
@RequestMapping("/api/s3")
@RestController
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping
    public String upload(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        return s3Service.upload(file);
    }

    @DeleteMapping
    public void delete(@RequestParam @URL String savedFileUrl) {
        s3Service.delete(savedFileUrl);
    }

}
