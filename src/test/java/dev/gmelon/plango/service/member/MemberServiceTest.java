package dev.gmelon.plango.service.member;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberStatisticsResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class MemberServiceTest {

    private Member memberA;

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberA = Member.builder()
                .email("a@a.com")
                .password("passwordA")
                .name("nameA")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(memberA);
    }

    @Test
    void 나의_프로필_조회() {
        // when
        MemberProfileResponseDto response = memberService.getMyProfile(memberA.getId());

        // then
        assertThat(response.getEmail()).isEqualTo(memberA.getEmail());
        assertThat(response.getName()).isEqualTo(memberA.getName());
    }

    @Test
    void 나의_통계정보_조회() {
        // when
        MemberStatisticsResponseDto response = memberService.getMyStatistics(memberA.getId());

        // then
        assertThat(response.getScheduleCount()).isGreaterThanOrEqualTo(0);
        assertThat(response.getDoneScheduleCount())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(response.getScheduleCount());
        assertThat(response.getDiaryCount()).isGreaterThanOrEqualTo(0);
    }
}
