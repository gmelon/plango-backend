package dev.gmelon.plango.web.auth;

import dev.gmelon.plango.auth.dto.SessionMember;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.service.auth.AuthService;
import dev.gmelon.plango.service.auth.dto.LoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public void signup(@RequestBody @Valid SignupRequestDto requestDto) {
        authService.signup(requestDto);
    }

    @PostMapping("/login")
    public void login(@RequestBody @Valid LoginRequestDto requestDto, HttpServletRequest request) {
        Member loginedMember = authService.login(requestDto);
        SessionMember sessionMember = SessionMember.of(loginedMember);

        HttpSession session = request.getSession();
        session.setAttribute(SessionMember.SESSION_NAME, sessionMember);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

}
