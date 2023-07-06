package dev.gmelon.plango.web.s3;

import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.web.TestAuthUtil;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class S3ControllerTest {

    private Cookie loginCookieOfMemberA;

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
        MultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                ContentType.IMAGE_JPEG.toString(),
                InputStream.nullInputStream()
        );

        ExtractableResponse<Response> response = RestAssured
                .given()
                .cookie(loginCookieOfMemberA)
                // TODO ByteArrayInputStream 대체제가 있을지
                .multiPart("file", "image.jpg", new ByteArrayInputStream(" ".getBytes()))
                .log().all()
                .when().post("/api/v1/s3")
                .then().log().all().extract();

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
                .when().post("/api/v1/s3")
                .then().log().all().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
