package dev.gmelon.plango.global.infrastructure.s3;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gmelon.plango.global.config.s3.AmazonS3TestImpl;
import java.io.InputStream;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class S3RepositoryTest {

    @Autowired
    private S3Repository s3Repository;
    @Autowired
    private AmazonS3TestImpl amazonS3;

    @Test
    void 파일_생성_요청() {
        // given, when
        String savedFileName = s3Repository.upload("image.jpg", InputStream.nullInputStream(), ContentType.IMAGE_JPEG.toString(), 0L);

        // then
        assertThat(savedFileName).startsWith("https://plango-backend");
        assertThat(savedFileName).endsWith(".jpg");
        assertThat(amazonS3.isFileSaved()).isTrue();
    }

    @Test
    void 파일_삭제_요청() {
        // given
        String savedFilePath = s3Repository.upload("image.jpg", InputStream.nullInputStream(), ContentType.IMAGE_JPEG.toString(), 0L);

        // when
        s3Repository.delete(savedFilePath);

        // then
        assertThat(amazonS3.isFileSaved()).isFalse();
    }
}
