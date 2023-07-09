package dev.gmelon.plango.web;

import dev.gmelon.plango.config.auth.dto.LoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import io.restassured.RestAssured;
import io.restassured.config.SessionConfig;
import io.restassured.http.Cookie;
import org.springframework.http.MediaType;

public class TestAuthUtil {

    public static Cookie signupAndGetCookie(SignupRequestDto requestDto) {
        signup(requestDto);
        String cookieValue = loginAndGetCookie(requestDto);

        return new Cookie.Builder(SessionConfig.DEFAULT_SESSION_ID_NAME, cookieValue).build();
    }

    private static void signup(SignupRequestDto requestDto) {
        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(requestDto).log().all()
                .when().post("/api/v1/auth/signup")
                .then().log().all();
    }

    private static String loginAndGetCookie(SignupRequestDto signupRequestDto) {
        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .email(signupRequestDto.getEmail())
                .password(signupRequestDto.getPassword())
                .build();

        return RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginRequestDto).log().all()
                .when().post("/api/v1/auth/login")
                .thenReturn().sessionId();
    }

}
