package dev.gmelon.plango;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.place.PlaceSearchRecord;
import dev.gmelon.plango.domain.place.PlaceSearchRecordRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.auth.AuthService;
import dev.gmelon.plango.service.auth.dto.SignupRequestDto;
import dev.gmelon.plango.service.schedule.ScheduleService;
import dev.gmelon.plango.service.schedule.dto.ScheduleCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Profile("local")
@RequiredArgsConstructor
@Component
public class TestDataInit {

    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final ScheduleService scheduleService;
    private final PlaceSearchRecordRepository placeSearchRecordRepository;
    private final ScheduleRepository scheduleRepository;

    @PostConstruct
    public void dataInit() {
        SignupRequestDto memberRequestA = SignupRequestDto.builder()
                .email("hsh1769@naver.com")
                .password("1111")
                .nickname("gmelon A")
                .profileImageUrl("https://avatars.githubusercontent.com/u/33623106?v=4")
                .build();
        authService.signup(memberRequestA);
        Member memberA = memberRepository.findByEmail(memberRequestA.getEmail()).get();

        SignupRequestDto memberRequestB = SignupRequestDto.builder()
                .email("hsh1769@daum.net")
                .password("1111")
                .nickname("gmelon B")
                .build();
        authService.signup(memberRequestB);
        Member memberB = memberRepository.findByEmail(memberRequestB.getEmail()).get();

        SignupRequestDto memberRequestC = SignupRequestDto.builder()
                .email("hsh1769@kakao.com")
                .password("1111")
                .nickname("gmelon C")
                .build();
        authService.signup(memberRequestC);
        Member memberC = memberRepository.findByEmail(memberRequestC.getEmail()).get();

        // memberA 개인 일정
        ScheduleCreateRequestDto scheduleRequestA = ScheduleCreateRequestDto.builder()
                .title("A 개인 일정")
                .content("일정 본문")
                .date(LocalDate.now())
                .startTime(LocalTime.now().withMinute(0))
                .endTime(LocalTime.now().plusHours(3).withMinute(0))
                .latitude(36.3674097)
                .longitude(127.3454477)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 공과대학 5호관")
                .participantIds(List.of())
                .build();
        scheduleService.create(memberA.getId(), scheduleRequestA);

        // memberA(owner) -> memberB 공유 일정
        ScheduleCreateRequestDto scheduleRequestB = ScheduleCreateRequestDto.builder()
                .title("A -> B 공유 일정")
                .content("일정 본문")
                .date(LocalDate.now())
                .startTime(LocalTime.now().plusHours(2).withMinute(0))
                .endTime(LocalTime.now().plusHours(5).withMinute(0))
                .latitude(36.3682999)
                .longitude(127.3420364)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 인문대학")
                .participantIds(List.of(memberB.getId()))
                .build();
        scheduleService.create(memberA.getId(), scheduleRequestB);

        // memberB(owner) -> memberA 공유 일정
        ScheduleCreateRequestDto scheduleRequestC = ScheduleCreateRequestDto.builder()
                .title("B -> A 공유 일정")
                .content("일정 본문")
                .date(LocalDate.now())
                .startTime(LocalTime.now().plusHours(2).withMinute(0))
                .endTime(LocalTime.now().plusHours(5).withMinute(0))
                .latitude(36.3682999)
                .longitude(127.3420364)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 인문대학")
                .participantIds(List.of(memberA.getId()))
                .build();
        scheduleService.create(memberB.getId(), scheduleRequestC);

        // memberA(owner) -> memberB, memberC 공유 일정
        ScheduleCreateRequestDto scheduleRequestD = ScheduleCreateRequestDto.builder()
                .title("A -> B,C 공유 일정")
                .content("일정 본문")
                .date(LocalDate.now())
                .startTime(LocalTime.now().plusHours(2).withMinute(0))
                .endTime(LocalTime.now().plusHours(5).withMinute(0))
                .latitude(36.3682999)
                .longitude(127.3420364)
                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                .placeName("충남대학교 인문대학")
                .participantIds(List.of())
                .build();
        Long createdScheduleDId = scheduleService.create(memberA.getId(), scheduleRequestD);
        // memberB는 수락 완료, memberC는 수락 대기
        Schedule scheduleD = scheduleRepository.findByIdWithScheduleMembers(createdScheduleDId).get();
        scheduleD.getScheduleMembers().add(ScheduleMember.builder().member(memberB).accepted(true).schedule(scheduleD).build());
        scheduleD.getScheduleMembers().add(ScheduleMember.builder().member(memberC).accepted(false).schedule(scheduleD).build());
        scheduleRepository.save(scheduleD);

        List<PlaceSearchRecord> placeSearchRecords = IntStream.rangeClosed(1, 60)
                .mapToObj(value -> PlaceSearchRecord.builder()
                        .keyword("검색어 " + value)
                        .lastSearchedDate(LocalDateTime.now().minusDays(value))
                        .member(memberA)
                        .build()
                )
                .collect(Collectors.toList());
        placeSearchRecordRepository.saveAll(placeSearchRecords);
    }

}
