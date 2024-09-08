package dev.gmelon.plango.global.util;

import dev.gmelon.plango.domain.auth.service.AuthService;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.entity.MemberRole;
import dev.gmelon.plango.domain.member.entity.MemberType;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.place.entity.PlaceSearchRecord;
import dev.gmelon.plango.domain.place.repository.PlaceSearchRecordRepository;
import dev.gmelon.plango.domain.refreshtoken.repository.RefreshTokenRepository;
import dev.gmelon.plango.domain.schedule.dto.ScheduleCreateRequestDto;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.schedule.entity.ScheduleMember;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.service.ScheduleService;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceCreateRequestDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile("local")
@RequiredArgsConstructor
@Component
public class TestDataGenerator implements ApplicationRunner {
    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final ScheduleService scheduleService;
    private final PlaceSearchRecordRepository placeSearchRecordRepository;
    private final ScheduleRepository scheduleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        resetRedisRepositories();

        Member memberA = Member.builder()
                .email("test1@naver.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("gmelon A")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .termsAccepted(true)
                .build();
        memberRepository.save(memberA);

        Member memberB = Member.builder()
                .email("test2@daum.net")
                .password(passwordEncoder.encode("1111"))
                .nickname("gmelon B")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .termsAccepted(true)
                .build();
        memberRepository.save(memberB);

        Member memberC = Member.builder()
                .email("test3@gmelon.dev")
                .password(passwordEncoder.encode("1111"))
                .nickname("gmelon C")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .termsAccepted(true)
                .build();
        memberRepository.save(memberC);

        // memberA 개인 일정
        ScheduleCreateRequestDto scheduleRequestA = ScheduleCreateRequestDto.builder()
                .title("A 개인 일정")
                .content("일정 본문")
                .date(LocalDate.now())
                .startTime(LocalTime.now().withMinute(0))
                .endTime(LocalTime.now().plusHours(3).withMinute(0))
                .schedulePlaces(List.of(
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3674097)
                                .longitude(127.3454477)
                                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                                .placeName("충남대학교 공과대학 5호관")
                                .memo("장소 메모")
                                .category("수업")
                                .build(),
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3682999)
                                .longitude(127.3420364)
                                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                                .placeName("충남대학교 인문대학")
                                .memo("장소 메모")
                                .category("수업")
                                .build(),
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3645845)
                                .longitude(127.3412946)
                                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                                .placeName("카페 인터뷰")
                                .memo("장소 메모")
                                .category("카페")
                                .build(),
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3637837)
                                .longitude(127.3411926)
                                .roadAddress("대전광역시 유성구 한밭대로371번길 38")
                                .placeName("라꼬레 | La Core")
                                .memo("장소 메모")
                                .category("식당")
                                .build()
                ))
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
                .schedulePlaces(List.of(
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3682999)
                                .longitude(127.3420364)
                                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                                .placeName("충남대학교 인문대학")
                                .memo("장소 메모")
                                .category("수업")
                                .build()
                ))
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
                .schedulePlaces(List.of())
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
                .schedulePlaces(List.of(
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3645845)
                                .longitude(127.3412946)
                                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                                .placeName("카페 인터뷰")
                                .memo("장소 메모")
                                .category("카페")
                                .build(),
                        SchedulePlaceCreateRequestDto.builder()
                                .latitude(36.3674097)
                                .longitude(127.3454477)
                                .roadAddress("대전광역시 유성구 온천2동 대학로 99")
                                .placeName("충남대학교 공과대학 5호관")
                                .category("수업")
                                .build()
                ))
                .participantIds(List.of())
                .build();
        Long createdScheduleDId = scheduleService.create(memberA.getId(), scheduleRequestD);
        // memberB는 수락 완료, memberC는 수락 대기
        Schedule scheduleD = scheduleRepository.findByIdWithMembers(createdScheduleDId).get();
        scheduleD.getScheduleMembers()
                .add(ScheduleMember.builder().member(memberB).accepted(true).schedule(scheduleD).build());
        scheduleD.getScheduleMembers()
                .add(ScheduleMember.builder().member(memberC).accepted(false).schedule(scheduleD).build());
        scheduleRepository.save(scheduleD);

        List<PlaceSearchRecord> placeSearchRecords = IntStream.rangeClosed(1, 10)
                .mapToObj(value -> PlaceSearchRecord.builder()
                        .keyword("검색어 " + value)
                        .lastSearchedDate(LocalDateTime.now().minusDays(value))
                        .member(memberA)
                        .build()
                )
                .collect(Collectors.toList());
        placeSearchRecordRepository.saveAll(placeSearchRecords);
    }

    private void resetRedisRepositories() {
        refreshTokenRepository.deleteAll();
    }
}
