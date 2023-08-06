package dev.gmelon.plango.web.s3;

import dev.gmelon.plango.exception.s3.EmptyFileException;
import dev.gmelon.plango.service.s3.S3Service;
import dev.gmelon.plango.service.s3.dto.FileDeleteRequestDto;
import dev.gmelon.plango.service.s3.dto.FileUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

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
