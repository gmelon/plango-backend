package dev.gmelon.plango.web.auth;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.auth.AuthService;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

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
