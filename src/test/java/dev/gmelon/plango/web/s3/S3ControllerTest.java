package dev.gmelon.plango.web.s3;

import dev.gmelon.plango.config.s3.AmazonS3TestImpl;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.web.TestAuthUtil;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class S3ControllerTest {

    private Cookie loginCookieOfMemberA;

    @Autowired
    private S3Repository s3Repository;
    @Autowired
    private AmazonS3TestImpl amazonS3;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        SignupRequestDto memberASignupRequest = SignupRequestDto.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .build();
        loginCookieOfMemberA = TestAuthUtil.signupAndGetCookie(memberASignupRequest);
    }

    @Test
    void 파일_저장_요청() throws IOException {
        // given, when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberA)
                // TODO ByteArrayInputStream 대체제가 있을지
                .multiPart("file", "image.jpg", new ByteArrayInputStream(" ".getBytes()))
                .log().all()
                .when().post("/api/s3")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 빈_파일로_파일_저장_요청() {
        // given, when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberA)
                .multiPart("file", "image.jpg", InputStream.nullInputStream())
                .log().all()
                .when().post("/api/s3")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 파일_삭제_요청() {
        // given
        String savedFileUrl = s3Repository.upload(
                "image.jpg",
                InputStream.nullInputStream(),
                ContentType.IMAGE_JPEG.toString(),
                0L
        );

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("savedFileUrl", savedFileUrl)
                .cookie(loginCookieOfMemberA)
                .log().all()
                .when().delete("/api/s3")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(amazonS3.isFileSaved()).isFalse();
    }

    @Test
    void 잘못된_url로_파일_삭제_요청() {
        // given
        s3Repository.upload(
                "image.jpg",
                InputStream.nullInputStream(),
                ContentType.IMAGE_JPEG.toString(),
                0L
        );

        // when
        ExtractableResponse<Response> response = RestAssured
                .given()
                .param("savedFileUrl", "invalid url")
                .cookie(loginCookieOfMemberA)
                .log().all()
                .when().delete("/api/s3")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(amazonS3.isFileSaved()).isTrue();
    }
}
