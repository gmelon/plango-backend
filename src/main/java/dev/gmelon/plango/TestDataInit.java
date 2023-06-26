package dev.gmelon.plango;

import dev.gmelon.plango.service.auth.AuthService;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Component
public class TestDataInit {

    private final AuthService authService;

    @PostConstruct
    public void memberDataInit() {
        SignupRequestDto request = SignupRequestDto.builder()
                .email("hsh1769@naver.com")
                .password("1234")
                .name("현상혁")
                .build();

        authService.signup(request);
    }

}
