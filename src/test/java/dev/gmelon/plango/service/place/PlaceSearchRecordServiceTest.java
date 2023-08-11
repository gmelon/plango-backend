package dev.gmelon.plango.service.place;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.place.PlaceSearchRecord;
import dev.gmelon.plango.domain.place.PlaceSearchRecordRepository;
import dev.gmelon.plango.service.place.dto.PlaceSearchRecordListResponseDto;
import dev.gmelon.plango.service.place.dto.PlaceSearchRecordRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "classpath:/reset.sql")
@SpringBootTest
class PlaceSearchRecordServiceTest {

    private Member member;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PlaceSearchRecordService placeSearchRecordService;
    @Autowired
    private PlaceSearchRecordRepository placeSearchRecordRepository;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("a@a.com")
                .password("passwordA")
                .nickname("nameA")
                .role(MemberRole.ROLE_USER)
                .build();
        memberRepository.save(member);
    }

    @Test
    void 현재_회원의_최근_검색어_목록_조회() {
        // given
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
        List<PlaceSearchRecordListResponseDto> firstPageResponseDtos = placeSearchRecordService.findAll(member.getId(), 1);
        List<PlaceSearchRecordListResponseDto> secondPageResponseDtos = placeSearchRecordService.findAll(member.getId(), 2);

        // then
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

    @Test
    void 최근_검색어_목록은_검색일_기준_내림차순으로_정렬() {
        // given
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
        List<PlaceSearchRecordListResponseDto> responseDtos = placeSearchRecordService.findAll(member.getId(), 1);

        // then
        List<String> expectedKeywords = List.of("검색어 3", "검색어 2", "검색어 1");
        assertThat(responseDtos)
                .extracting(PlaceSearchRecordListResponseDto::getKeyword)
                .isEqualTo(expectedKeywords);
    }

    @Test
    void 새로운_키워드_검색_시_최근_검색어_목록에_추가() {
        // given
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
        placeSearchRecordService.search(member.getId(), requestDto);

        // then
        List<PlaceSearchRecord> foundPlaceSearchRecords = placeSearchRecordRepository.findAllByMemberId(member.getId(), 1);
        assertThat(foundPlaceSearchRecords).hasSize(3);
        assertThat(placeSearchRecordRepository.findByKeywordAndMemberId(requestKeyword, member.getId())).isPresent();
    }

    @Test
    void 기존_키워드_재검색_시_날짜만_수정() {
        // given
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
        placeSearchRecordService.search(member.getId(), requestDto);

        // then
        List<PlaceSearchRecord> foundPlaceSearchRecords = placeSearchRecordRepository.findAllByMemberId(member.getId(), 1);
        assertThat(foundPlaceSearchRecords).hasSize(2);

        PlaceSearchRecord foundPlaceSearchRecord = assertDoesNotThrow(() -> placeSearchRecordRepository.findByKeywordAndMemberId(requestKeyword, member.getId()).get());
        assertThat(foundPlaceSearchRecord.getLastSearchedDate().toLocalDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void 최근_검색어_삭제() {
        // given
        String givenKeyword = "강남역";
        PlaceSearchRecord givenPlaceSearchRecord = PlaceSearchRecord.builder()
                .member(member)
                .keyword(givenKeyword)
                .lastSearchedDate(LocalDateTime.now().minusDays(2))
                .build();
        placeSearchRecordRepository.save(givenPlaceSearchRecord);

        // when
        placeSearchRecordService.delete(member.getId(), givenPlaceSearchRecord.getId());

        // then
        assertThat(placeSearchRecordRepository.findByKeywordAndMemberId(givenKeyword, member.getId())).isEmpty();
    }

}
