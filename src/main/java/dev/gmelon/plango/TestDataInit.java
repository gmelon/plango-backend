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
import java.time.LocalDate;
import java.time.LocalTime;
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
                .password("1111")
                .nickname("gmelon")
                .profileImageUrl("https://avatars.githubusercontent.com/u/33623106?v=4")
                .build();
        authService.signup(request);
        Member memberA = memberRepository.findByEmail(request.getEmail()).get();

        List<Schedule> schedules = List.of(Schedule.builder()
                        .title("일정 제목 1")
                        .content("일정 본문 1")
                        .date(LocalDate.now())
                        .startTime(LocalTime.now().withMinute(0))
                        .endTime(LocalTime.now().plusHours(3).withMinute(0))
                        .latitude(36.3674097)
                        .longitude(127.3454477)
                        .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                        .placeName("충남대학교 공과대학 5호관")
                        .member(memberA)
                        .build(),
                Schedule.builder()
                        .title("일정 제목 2")
                        .content("일정 본문 2")
                        .date(LocalDate.now())
                        .startTime(LocalTime.now().plusHours(2).withMinute(0))
                        .endTime(LocalTime.now().plusHours(5).withMinute(0))
                        .latitude(36.3682999)
                        .longitude(127.3420364)
                        .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                        .placeName("충남대학교 인문대학")
                        .member(memberA)
                        .build());
        scheduleRepository.saveAll(schedules);
    }

}
