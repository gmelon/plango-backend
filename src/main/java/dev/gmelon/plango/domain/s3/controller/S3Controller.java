package dev.gmelon.plango.domain.s3.controller;

import dev.gmelon.plango.domain.s3.dto.FileDeleteRequestDto;
import dev.gmelon.plango.domain.s3.dto.FileUploadResponseDto;
import dev.gmelon.plango.domain.s3.exception.EmptyFileException;
import dev.gmelon.plango.domain.s3.service.S3Service;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api/s3")
@RestController
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping
    public FileUploadResponseDto uploadAll(@RequestParam List<MultipartFile> files) {
        validateFilesAreNotEmpty(files);
        return s3Service.uploadAll(files);
    }

    private void validateFilesAreNotEmpty(List<MultipartFile> files) {
        boolean emptyFileExists = files.stream()
                .anyMatch(MultipartFile::isEmpty);
        if (emptyFileExists) {
            throw new EmptyFileException();
        }
    }

    @DeleteMapping
    public void deleteAll(@RequestBody @Valid FileDeleteRequestDto requestDto) {
        s3Service.deleteAll(requestDto);
    }

}
