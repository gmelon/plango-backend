package dev.gmelon.plango.domain.auth.controller;

import dev.gmelon.plango.domain.auth.dto.CheckEmailTokenRequestDto;
import dev.gmelon.plango.domain.auth.dto.LoginRequestDto;
import dev.gmelon.plango.domain.auth.dto.PasswordResetRequestDto;
import dev.gmelon.plango.domain.auth.dto.SendEmailTokenRequestDto;
import dev.gmelon.plango.domain.auth.dto.SignupRequestDto;
import dev.gmelon.plango.domain.auth.dto.SnsLoginRequestDto;
import dev.gmelon.plango.domain.auth.dto.SnsRevokeRequestDto;
import dev.gmelon.plango.domain.auth.dto.TokenRefreshRequestDto;
import dev.gmelon.plango.domain.auth.dto.TokenResponseDto;
import dev.gmelon.plango.domain.auth.service.AuthService;
import dev.gmelon.plango.global.config.auth.LoginMember;
import dev.gmelon.plango.global.dto.StatusResponseDto;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody @Valid LoginRequestDto requestDto) {
        return authService.login(requestDto);
    }

    @PostMapping("/sns-login")
    public TokenResponseDto snsLogin(@RequestBody @Valid SnsLoginRequestDto requestDto) {
        return authService.snsLogin(requestDto);
    }

    @PostMapping("/token-refresh")
    public TokenResponseDto tokenRefresh(@RequestBody @Valid TokenRefreshRequestDto requestDto) {
        return authService.tokenRefresh(requestDto);
    }

    @PostMapping("/logout")
    public void logout(@LoginMember Long memberId) {
        authService.logout(memberId);
    }

    @DeleteMapping("/sns-revoke")
    public void snsRevoke(@RequestBody @Valid SnsRevokeRequestDto requestDto) {
        authService.snsRevoke(requestDto);
    }

    @PostMapping("send-email-token")
    public void sendEmailToken(@RequestBody @Valid SendEmailTokenRequestDto requestDto) {
        authService.sendEmailToken(requestDto);
    }

    @GetMapping("check-email-token")
    public ResponseEntity<StatusResponseDto> checkEmailToken(
            @ModelAttribute @Valid CheckEmailTokenRequestDto requestDto) {
        StatusResponseDto responseDto = authService.checkEmailToken(requestDto);
        return ResponseEntity
                .status(responseDto.getCode())
                .body(responseDto);
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

    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody @Valid PasswordResetRequestDto requestDto) {
        authService.resetPassword(requestDto);
    }

}
