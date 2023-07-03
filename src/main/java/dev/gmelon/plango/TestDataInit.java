package dev.gmelon.plango;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.auth.AuthService;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Profile("local")
@RequiredArgsConstructor
@Component
public class TestDataInit {

    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;

    @PostConstruct
    public void dataInit() {
        SignupRequestDto request = SignupRequestDto.builder()
                .email("hsh1769@naver.com")
                .password("1234")
                .name("현상혁")
                .build();
        authService.signup(request);
        Member memberA = memberRepository.findByEmail(request.getEmail()).get();

        List<Schedule> schedules = List.of(Schedule.builder()
                        .title("계획 제목 1")
                        .content("계획 본문 1")
                        .startTime(LocalDateTime.now().withMinute(0))
                        .endTime(LocalDateTime.now().plusHours(3).withMinute(0))
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("계획 제목 2")
                        .content("계획 본문 2")
                        .startTime(LocalDateTime.now().plusHours(2).withMinute(0))
                        .endTime(LocalDateTime.now().plusHours(5).withMinute(0))
                        .member(memberA)
                        .build());
        scheduleRepository.saveAll(schedules);

        Class<Integer> c = Integer.class;
    }

}
