package dev.gmelon.plango.web.fcm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageToken;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.member.MemberType;
import dev.gmelon.plango.service.fcm.dto.FirebaseCloudMessageTokenCreateOrUpdateRequestDto;
import dev.gmelon.plango.service.fcm.dto.FirebaseCloudMessageTokenDeleteRequestDto;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class FirebaseCloudMessageTokenControllerTest {

    private static final String TEST_TOKEN_VALUE = "123-abc-456-def";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;
    @Autowired
    private MemberRepository memberRepository;

    @PlangoMockUser
    @Test
    void 토큰을_생성한다() throws Exception {
        // given
        FirebaseCloudMessageTokenCreateOrUpdateRequestDto request = FirebaseCloudMessageTokenCreateOrUpdateRequestDto.builder()
                .tokenValue(TEST_TOKEN_VALUE)
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/fcm/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(firebaseCloudMessageTokenRepository.findByTokenValue(TEST_TOKEN_VALUE)).isPresent();
    }

    @PlangoMockUser
    @Test
    void 이미_존재하는_토큰일경우_lastUpdatedDate를_갱신한다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        FirebaseCloudMessageToken givenToken = FirebaseCloudMessageToken.builder()
                .tokenValue(TEST_TOKEN_VALUE)
                .member(member)
                .lastUpdatedDate(LocalDateTime.of(2023, 6, 1, 0, 0, 0))
                .build();
        firebaseCloudMessageTokenRepository.save(givenToken);

        FirebaseCloudMessageTokenCreateOrUpdateRequestDto request = FirebaseCloudMessageTokenCreateOrUpdateRequestDto.builder()
                .tokenValue(TEST_TOKEN_VALUE)
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/fcm/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        FirebaseCloudMessageToken foundToken = firebaseCloudMessageTokenRepository.findByTokenValue(TEST_TOKEN_VALUE).get();
        assertThat(foundToken.getLastUpdatedDate()).isAfter(givenToken.getLastUpdatedDate());
    }

    @PlangoMockUser
    @Test
    void 토큰을_삭제한다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        FirebaseCloudMessageToken givenToken = FirebaseCloudMessageToken.builder()
                .tokenValue(TEST_TOKEN_VALUE)
                .member(member)
                .lastUpdatedDate(LocalDateTime.of(2023, 6, 1, 0, 0, 0))
                .build();
        firebaseCloudMessageTokenRepository.save(givenToken);

        FirebaseCloudMessageTokenDeleteRequestDto request = FirebaseCloudMessageTokenDeleteRequestDto.builder()
                .tokenValue(TEST_TOKEN_VALUE)
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/fcm/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(firebaseCloudMessageTokenRepository.findByTokenValue(TEST_TOKEN_VALUE)).isEmpty();
    }

    @PlangoMockUser
    @Test
    void 다른_사용자의_토큰일경우_404에러가_발생한다() throws Exception {
        // given
        Member anotherMember = createAnotherMember();

        FirebaseCloudMessageToken givenToken = FirebaseCloudMessageToken.builder()
                .tokenValue(TEST_TOKEN_VALUE)
                .member(anotherMember)
                .lastUpdatedDate(LocalDateTime.of(2023, 6, 1, 0, 0, 0))
                .build();
        firebaseCloudMessageTokenRepository.save(givenToken);

        FirebaseCloudMessageTokenDeleteRequestDto request = FirebaseCloudMessageTokenDeleteRequestDto.builder()
                .tokenValue(TEST_TOKEN_VALUE)
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/fcm/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(firebaseCloudMessageTokenRepository.findByTokenValue(TEST_TOKEN_VALUE)).isPresent();
    }

    private Member createAnotherMember() {
        Member member = Member.builder()
                .email("b@b.com")
                .password("passwordB")
                .nickname("nameB")
                .role(MemberRole.ROLE_USER)
                .type(MemberType.EMAIL)
                .build();
        return memberRepository.save(member);
    }
}
