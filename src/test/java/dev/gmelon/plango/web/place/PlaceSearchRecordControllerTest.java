package dev.gmelon.plango.web.place;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.config.security.PlangoMockUser;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.place.PlaceSearchRecord;
import dev.gmelon.plango.domain.place.PlaceSearchRecordRepository;
import dev.gmelon.plango.service.place.dto.PlaceSearchRecordListResponseDto;
import dev.gmelon.plango.service.place.dto.PlaceSearchRecordRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@Sql(value = "classpath:/reset.sql")
@AutoConfigureMockMvc
@SpringBootTest
class PlaceSearchRecordControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceSearchRecordRepository placeSearchRecordRepository;
    @Autowired
    private MemberRepository memberRepository;

    @PlangoMockUser
    @Test
    void 현재_회원의_최근_검색어_목록_조회() throws Exception {
        // given
        final Member member = memberRepository.findAll().get(0);

        List<PlaceSearchRecord> placeSearchRecords = IntStream.rangeClosed(1, 50)
                .mapToObj(value -> PlaceSearchRecord.builder()
                        .keyword(String.valueOf(value))
                        .lastSearchedDate(LocalDateTime.now().minusDays(value))
                        .member(member)
                        .build()
                )
                .collect(Collectors.toList());
        placeSearchRecordRepository.saveAll(placeSearchRecords);

        // when
        MockHttpServletResponse firstPageResponse = mockMvc.perform(get("/api/place/search-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "1"))
                .andReturn().getResponse();

        MockHttpServletResponse secondPageResponse = mockMvc.perform(get("/api/place/search-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "2"))
                .andReturn().getResponse();

        // then
        assertThat(firstPageResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<PlaceSearchRecordListResponseDto> firstPageResponseDtos = Arrays.asList(objectMapper.readValue(firstPageResponse.getContentAsString(UTF_8), PlaceSearchRecordListResponseDto[].class));
        List<PlaceSearchRecordListResponseDto> secondPageResponseDtos = Arrays.asList(objectMapper.readValue(secondPageResponse.getContentAsString(UTF_8), PlaceSearchRecordListResponseDto[].class));
        List<String> expectedFirstPageKeywords = IntStream.rangeClosed(1, 40)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        List<String> expectedSecondPageKeywords = IntStream.rangeClosed(41, 50)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        assertThat(firstPageResponseDtos)
                .extracting(PlaceSearchRecordListResponseDto::getKeyword)
                .containsExactlyInAnyOrderElementsOf(expectedFirstPageKeywords);
        assertThat(secondPageResponseDtos)
                .extracting(PlaceSearchRecordListResponseDto::getKeyword)
                .containsExactlyInAnyOrderElementsOf(expectedSecondPageKeywords);
    }

    @PlangoMockUser
    @Test
    void 페이지_파라미터가_0이면_첫번째_페이지_조회() throws Exception {
        // given
        final Member member = memberRepository.findAll().get(0);

        List<PlaceSearchRecord> placeSearchRecords = IntStream.rangeClosed(1, 50)
                .mapToObj(value -> PlaceSearchRecord.builder()
                        .keyword(String.valueOf(value))
                        .lastSearchedDate(LocalDateTime.now().minusDays(value))
                        .member(member)
                        .build()
                )
                .collect(Collectors.toList());
        placeSearchRecordRepository.saveAll(placeSearchRecords);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/place/search-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<PlaceSearchRecordListResponseDto> responseDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(UTF_8), PlaceSearchRecordListResponseDto[].class));
        List<String> expectedFirstPageKeywords = IntStream.rangeClosed(1, 40)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        assertThat(responseDtos)
                .extracting(PlaceSearchRecordListResponseDto::getKeyword)
                .containsExactlyInAnyOrderElementsOf(expectedFirstPageKeywords);

    }

    @PlangoMockUser
    @Test
    void 페이지_파라미터가_없으면_첫번째_페이지_조회() throws Exception {
        // given
        final Member member = memberRepository.findAll().get(0);

        List<PlaceSearchRecord> placeSearchRecords = IntStream.rangeClosed(1, 50)
                .mapToObj(value -> PlaceSearchRecord.builder()
                        .keyword(String.valueOf(value))
                        .lastSearchedDate(LocalDateTime.now().minusDays(value))
                        .member(member)
                        .build()
                )
                .collect(Collectors.toList());
        placeSearchRecordRepository.saveAll(placeSearchRecords);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/place/search-records")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<PlaceSearchRecordListResponseDto> responseDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(UTF_8), PlaceSearchRecordListResponseDto[].class));
        List<String> expectedFirstPageKeywords = IntStream.rangeClosed(1, 40)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        assertThat(responseDtos)
                .extracting(PlaceSearchRecordListResponseDto::getKeyword)
                .containsExactlyInAnyOrderElementsOf(expectedFirstPageKeywords);
    }

    @PlangoMockUser
    @Test
    void 최근_검색어_목록은_검색일_기준_내림차순으로_정렬() throws Exception {
        // given
        final Member member = memberRepository.findAll().get(0);

        List<PlaceSearchRecord> placeSearchRecords = List.of(
                PlaceSearchRecord.builder()
                        .member(member)
                        .keyword("검색어 1")
                        .lastSearchedDate(LocalDateTime.now().minusDays(3))
                        .build(),
                PlaceSearchRecord.builder()
                        .member(member)
                        .keyword("검색어 2")
                        .lastSearchedDate(LocalDateTime.now().minusDays(2))
                        .build(),
                PlaceSearchRecord.builder()
                        .member(member)
                        .keyword("검색어 3")
                        .lastSearchedDate(LocalDateTime.now().minusDays(1))
                        .build()
        );
        placeSearchRecordRepository.saveAll(placeSearchRecords);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/place/search-records")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<PlaceSearchRecordListResponseDto> responseDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(UTF_8), PlaceSearchRecordListResponseDto[].class));
        List<String> expectedKeywords = List.of("검색어 3", "검색어 2", "검색어 1");
        assertThat(responseDtos)
                .extracting(PlaceSearchRecordListResponseDto::getKeyword)
                .isEqualTo(expectedKeywords);
    }

    @PlangoMockUser
    @Test
    void 새로운_키워드_검색_시_최근_검색어_목록에_추가() throws Exception {
        // given
        final Member member = memberRepository.findAll().get(0);

        List<PlaceSearchRecord> placeSearchRecords = List.of(
                PlaceSearchRecord.builder()
                        .member(member)
                        .keyword("강남역")
                        .build(),
                PlaceSearchRecord.builder()
                        .member(member)
                        .keyword("수서역")
                        .build()
        );
        placeSearchRecordRepository.saveAll(placeSearchRecords);

        String requestKeyword = "판교역";
        PlaceSearchRecordRequestDto requestDto = PlaceSearchRecordRequestDto.builder().keyword(requestKeyword).build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/place/search-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<PlaceSearchRecord> foundPlaceSearchRecords = placeSearchRecordRepository.findAllByMemberId(member.getId(), 1);
        assertThat(foundPlaceSearchRecords).hasSize(3);
        assertThat(placeSearchRecordRepository.findByKeywordAndMemberId(requestKeyword, member.getId())).isPresent();
    }

    @PlangoMockUser
    @Test
    void 기존_키워드_재검색_시_날짜만_수정() throws Exception {
        // given
        final Member member = memberRepository.findAll().get(0);

        List<PlaceSearchRecord> placeSearchRecords = List.of(
                PlaceSearchRecord.builder()
                        .member(member)
                        .keyword("강남역")
                        .lastSearchedDate(LocalDateTime.now().minusDays(2))
                        .build(),
                PlaceSearchRecord.builder()
                        .member(member)
                        .keyword("수서역")
                        .lastSearchedDate(LocalDateTime.now().minusDays(2))
                        .build()
        );
        placeSearchRecordRepository.saveAll(placeSearchRecords);

        String requestKeyword = "강남역";
        PlaceSearchRecordRequestDto requestDto = PlaceSearchRecordRequestDto.builder().keyword(requestKeyword).build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/place/search-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<PlaceSearchRecord> foundPlaceSearchRecords = placeSearchRecordRepository.findAllByMemberId(member.getId(), 1);
        assertThat(foundPlaceSearchRecords).hasSize(2);

        PlaceSearchRecord foundPlaceSearchRecord = assertDoesNotThrow(() -> placeSearchRecordRepository.findByKeywordAndMemberId(requestKeyword, member.getId()).get());
        assertThat(foundPlaceSearchRecord.getLastSearchedDate().toLocalDate()).isEqualTo(LocalDate.now());
    }

    @PlangoMockUser
    @Test
    void 최근_검색어_삭제() throws Exception {
        // given
        final Member member = memberRepository.findAll().get(0);

        String givenKeyword = "강남역";
        PlaceSearchRecord givenPlaceSearchRecord = PlaceSearchRecord.builder()
                .member(member)
                .keyword(givenKeyword)
                .lastSearchedDate(LocalDateTime.now().minusDays(2))
                .build();
        placeSearchRecordRepository.save(givenPlaceSearchRecord);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/place/search-records/" + givenPlaceSearchRecord.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        assertThat(placeSearchRecordRepository.findByKeywordAndMemberId(givenKeyword, member.getId())).isEmpty();
    }

}
