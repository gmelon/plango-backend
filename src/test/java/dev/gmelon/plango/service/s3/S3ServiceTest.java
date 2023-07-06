package dev.gmelon.plango.service.s3;

import dev.gmelon.plango.domain.member.MemberRepository;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class S3ServiceTest {

    @Autowired
    private S3Service s3Service;
    @Autowired
    private MemberRepository memberRepository;

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
        String savedFilePath = s3Service.upload(file);

        // then
        assertThat(savedFilePath).startsWith("https://plango-backend");
        assertThat(savedFilePath).endsWith(".jpg");
    }
}
