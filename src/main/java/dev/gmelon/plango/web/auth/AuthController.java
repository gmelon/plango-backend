package dev.gmelon.plango.web.auth;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.auth.AuthService;
import dev.gmelon.plango.service.auth.dto.LoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.service.auth.dto.TokenRefreshRequestDto;
import dev.gmelon.plango.service.auth.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody @Valid LoginRequestDto requestDto) {
        return authService.login(requestDto);
    }

    @PostMapping("/logout")
    public void logout(@LoginMember Long memberId) {
        authService.logout(memberId);
    }

    @PostMapping("/token-refresh")
    public TokenResponseDto tokenRefresh(@RequestBody @Valid TokenRefreshRequestDto requestDto) {
        return authService.tokenRefresh(requestDto);
    }

    @PostMapping("/signup")
    public void signup(@RequestBody @Valid SignupRequestDto requestDto) {
        authService.signup(requestDto);
    }

    @DeleteMapping("/signout")
    public void signout(@LoginMember Long memberId) {
        authService.signout(memberId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/signout/{memberId}")
    public void signoutByAdmin(@PathVariable Long memberId) {
        authService.signout(memberId);
    }

}
