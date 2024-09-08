package dev.gmelon.plango.domain.s3.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.domain.s3.dto.FileDeleteRequestDto;
import dev.gmelon.plango.global.config.s3.AmazonS3TestImpl;
import dev.gmelon.plango.global.config.security.PlangoMockUser;
import dev.gmelon.plango.global.infrastructure.s3.S3Repository;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class S3ControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private S3Repository s3Repository;
    @Autowired
    private AmazonS3TestImpl amazonS3;

    @PlangoMockUser
    @Test
    void 파일_저장_요청() throws Exception {
        // given
        MockMultipartFile givenFile = new MockMultipartFile("files", "image.jpg", ContentType.IMAGE_JPEG.toString(), new ByteArrayInputStream(" ".getBytes()));

        // when
        MockHttpServletResponse response = mockMvc.perform(multipart("/api/s3")
                        .file(givenFile))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @PlangoMockUser
    @Test
    void 빈_파일로_파일_저장_요청() throws Exception {
        // given
        MockMultipartFile givenFile = new MockMultipartFile("file", "image.jpg", ContentType.IMAGE_JPEG.toString(), InputStream.nullInputStream());

        // when
        MockHttpServletResponse response = mockMvc.perform(multipart("/api/s3")
                        .file(givenFile))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 파일_삭제_요청() throws Exception {
        // given
        String savedFileUrl = s3Repository.upload(
                "image.jpg",
                InputStream.nullInputStream(),
                ContentType.IMAGE_JPEG.toString(),
                0L
        );

        FileDeleteRequestDto request = new FileDeleteRequestDto(List.of(savedFileUrl));

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/s3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(amazonS3.isFileSaved()).isFalse();
    }

    @PlangoMockUser
    @Test
    void 잘못된_url로_파일_삭제_요청() throws Exception {
        // given
        s3Repository.upload(
                "image.jpg",
                InputStream.nullInputStream(),
                ContentType.IMAGE_JPEG.toString(),
                0L
        );

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/s3")
                        .param("savedFileUrl", "invalid url"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(amazonS3.isFileSaved()).isTrue();
    }
}
