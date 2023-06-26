package dev.gmelon.plango.service.auth;

import dev.gmelon.plango.auth.PasswordEncoder;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.service.auth.dto.LoginRequestDto;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private static final String INVALID_LOGIN_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        boolean isEmailAlreadyExists = memberRepository.findByEmail(requestDto.getEmail())
                .isPresent();
        if (isEmailAlreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }

        String encodePassword = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setPassword(encodePassword);
        memberRepository.save(requestDto.toEntity());
    }

    public Member login(LoginRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(INVALID_LOGIN_MESSAGE));

        if (isPasswordIncorrect(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException(INVALID_LOGIN_MESSAGE);
        }

        return member;
    }

    private boolean isPasswordIncorrect(String requestPassword, String memberPassword) {
        return !passwordEncoder.matches(requestPassword, memberPassword);
    }

}
