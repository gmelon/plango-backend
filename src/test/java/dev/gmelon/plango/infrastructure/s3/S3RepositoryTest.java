package dev.gmelon.plango.infrastructure.s3;

import dev.gmelon.plango.config.s3.AmazonS3TestImpl;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

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
        String savedFileName = s3Repository.upload("image.jpg", InputStream.nullInputStream(), ContentType.IMAGE_JPEG.toString(), 0L);

        // when
        s3Repository.delete(savedFileName);

        // then
        assertThat(amazonS3.isFileSaved()).isFalse();
    }
}
