package dev.gmelon.plango.web.member;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.member.MemberType;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.service.member.dto.MemberEditProfileRequestDto;
import dev.gmelon.plango.service.member.dto.MemberProfileResponseDto;
import dev.gmelon.plango.service.member.dto.MemberSearchResponseDto;
import dev.gmelon.plango.service.member.dto.PasswordChangeRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class MemberControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PlangoMockUser
    @Test
    void 나의_프로필_조회() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/members/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        MemberProfileResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), MemberProfileResponseDto.class);
        Member member = memberRepository.findAll().get(0);

        assertThat(responseDto.getId()).isEqualTo(member.getId());
        assertThat(responseDto.getEmail()).isEqualTo(member.getEmail());
        assertThat(responseDto.getNickname()).isEqualTo(member.getNickname());
        assertThat(responseDto.getProfileImageUrl()).isEqualTo(member.getProfileImageUrl());
    }

    @PlangoMockUser
    @Test
    void 다른_회원의_프로필_조회() throws Exception {
        // given
        Member memberB = Member.builder()
                .email("b@b.com")
                .password(passwordEncoder.encode("passwordB"))
                .nickname("nameB")
                .bio("소개 B")
                .profileImageUrl("https://plango-backend/imageB.jpg")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .build();
        memberRepository.save(memberB);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/members/" + memberB.getId() + "/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        MemberProfileResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), MemberProfileResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(memberB.getId());
        assertThat(responseDto.getEmail()).isEqualTo(memberB.getEmail());
        assertThat(responseDto.getNickname()).isEqualTo(memberB.getNickname());
        assertThat(responseDto.getBio()).isEqualTo(memberB.getBio());
        assertThat(responseDto.getProfileImageUrl()).isEqualTo(memberB.getProfileImageUrl());
    }

    @PlangoMockUser
    @Test
    void 닉네임으로_프로필_검색() throws Exception {
        // given
        Member memberB = Member.builder()
                .email("b@b.com")
                .password(passwordEncoder.encode("passwordB"))
                .nickname("멤버 B 닉네임")
                .profileImageUrl("https://plango-backend/imageB.jpg")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .build();
        memberRepository.save(memberB);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/members")
                        .param("nickname", "버B닉"))
                .andReturn().getResponse();

        // then
        MemberSearchResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), MemberSearchResponseDto[].class);
        assertThat(responseDtos).hasSize(1);
        assertThat(responseDtos[0].getId()).isEqualTo(memberB.getId());
    }

    @PlangoMockUser
    @Test
    void 닉네임으로_프로필_검색시_자신은_검색되지_않음() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/members")
                        .param("nickname", "nameA"))
                .andReturn().getResponse();

        // then
        MemberSearchResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), MemberSearchResponseDto[].class);
        assertThat(responseDtos).hasSize(0);
    }

    @PlangoMockUser
    @Test
    void 닉네임으로_프로필_검색시_검색어가_없으면_예외발생() throws Exception {
        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/members")
                        .param("nickname", " ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @PlangoMockUser
    @Test
    void 비밀번호_변경() throws Exception {
        // given
        PasswordChangeRequestDto request = PasswordChangeRequestDto.builder()
                .previousPassword("passwordA")
                .newPassword("passwordB")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/members/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Member member = memberRepository.findAll().get(0);

        assertThat(passwordEncoder.matches(request.getNewPassword(), member.getPassword())).isTrue();
    }

    @PlangoMockUser
    @Test
    void 잘못된_이전_비밀번호로_비밀번호_변경() throws Exception {
        // given
        PasswordChangeRequestDto request = PasswordChangeRequestDto.builder()
                .previousPassword("passwordC")
                .newPassword("passwordB")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/members/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Member member = memberRepository.findAll().get(0);

        assertThat(passwordEncoder.matches("passwordA", member.getPassword())).isTrue();
    }

    @PlangoMockUser
    @Test
    void 프로필_수정() throws Exception {
        // given
        MemberEditProfileRequestDto request = MemberEditProfileRequestDto.builder()
                .nickname("nameB")
                .bio("소개 B")
                .profileImageUrl("https://plango-backend/imageB.jpg")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/members/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Member member = memberRepository.findAll().get(0);

        assertThat(member.getNickname()).isEqualTo(request.getNickname());
        assertThat(member.getBio()).isEqualTo(request.getBio());
        assertThat(member.getProfileImageUrl()).isEqualTo(request.getProfileImageUrl());
    }
}
