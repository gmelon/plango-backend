package dev.gmelon.plango.service.s3;

import dev.gmelon.plango.config.s3.AmazonS3TestImpl;
import dev.gmelon.plango.service.s3.dto.FileDeleteRequestDto;
import dev.gmelon.plango.service.s3.dto.FileUploadResponseDto;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class S3ServiceTest {

    @Autowired
    private S3Service s3Service;
    @Autowired
    private AmazonS3TestImpl amazonS3;

    @Test
    void 파일_저장_요청() throws IOException {
        // given
        MultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                ContentType.IMAGE_JPEG.toString(),
                InputStream.nullInputStream()
        );

        // when
        FileUploadResponseDto response = s3Service.uploadAll(List.of(file));

        // then
        String uploadedFileUrl = response.getUploadedFileUrls().get(0);
        assertThat(uploadedFileUrl).startsWith("https://plango-backend");
        assertThat(uploadedFileUrl).endsWith(".jpg");
        assertThat(amazonS3.isFileSaved()).isTrue();
    }

    @Test
    void 파일_삭제_요청() throws IOException {
        // given
        MultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                ContentType.IMAGE_JPEG.toString(),
                InputStream.nullInputStream()
        );
        FileUploadResponseDto uploadResponse = s3Service.uploadAll(List.of(file));

        FileDeleteRequestDto deleteRequest = new FileDeleteRequestDto(uploadResponse.getUploadedFileUrls());

        // when
        s3Service.deleteAll(deleteRequest);

        // then
        assertThat(amazonS3.isFileSaved()).isFalse();
    }
}
