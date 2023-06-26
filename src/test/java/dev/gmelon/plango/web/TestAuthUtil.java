package dev.gmelon.plango.web;

import dev.gmelon.plango.service.auth.dto.LoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import io.restassured.RestAssured;
import io.restassured.config.SessionConfig;
import org.springframework.http.MediaType;

import javax.servlet.http.Cookie;

public class TestAuthUtil {

    public static void signup(SignupRequestDto requestDto) {
        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(requestDto).log().all()
                .when().post("/api/v1/auth/signup")
                .then().log().all();
    }

    public static Cookie loginAndGetCookie(LoginRequestDto requestDto) {
        String cookieValue = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(requestDto).log().all()
                .when().post("/api/v1/auth/login")
                .thenReturn().sessionId();

        return new Cookie(SessionConfig.DEFAULT_SESSION_ID_NAME, cookieValue);
    }

}
