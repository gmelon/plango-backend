package dev.gmelon.plango.domain.scheduleplace.controller;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.entity.MemberRole;
import dev.gmelon.plango.domain.member.entity.MemberType;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.schedule.dto.ScheduleResponseDto;
import dev.gmelon.plango.domain.schedule.dto.ScheduleResponseDto.SchedulePlaceResponseDto;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.schedule.entity.ScheduleMember;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceCreateRequestDto;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceEditRequestDto;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceSearchResponseDto;
import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlace;
import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlaceLike;
import dev.gmelon.plango.domain.scheduleplace.repository.SchedulePlaceLikeRepository;
import dev.gmelon.plango.domain.scheduleplace.repository.SchedulePlaceRepository;
import dev.gmelon.plango.global.config.security.PlangoMockUser;
import java.util.List;
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
class SchedulePlaceControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private SchedulePlaceRepository schedulePlaceRepository;
    @Autowired
    private SchedulePlaceLikeRepository schedulePlaceLikeRepository;

    @PlangoMockUser
    @Test
    void like가_있는_confirm된_일정_장소_단건_조회() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setScheduleMembers(List.of(
                ScheduleMember.createOwner(member, givenSchedule),
                ScheduleMember.createParticipant(anotherMember, givenSchedule)
        ));
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(true)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        List<SchedulePlaceLike> givenSchedulePlaceLikes = List.of(SchedulePlaceLike.builder()
                        .schedulePlace(givenSchedulePlace)
                        .member(member)
                        .build(),
                SchedulePlaceLike.builder()
                        .schedulePlace(givenSchedulePlace)
                        .member(anotherMember)
                        .build());
        schedulePlaceLikeRepository.saveAll(givenSchedulePlaceLikes);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedules/{scheduleId}", givenSchedule.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        ScheduleResponseDto responseDto = objectMapper.readValue(response.getContentAsString(UTF_8), ScheduleResponseDto.class);
        SchedulePlaceResponseDto schedulePlaceResponseDto = responseDto.getSchedulePlaces().get(0);

        assertThat(schedulePlaceResponseDto.getIsConfirmed()).isTrue();
        assertThat(schedulePlaceResponseDto.getLikedMemberIds())
                .containsExactlyInAnyOrder(member.getId(), anotherMember.getId());
    }

    @PlangoMockUser
    @Test
    void 기존_일정에_장소를_추가한다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlaceCreateRequestDto createRequest = SchedulePlaceCreateRequestDto.builder()
                .latitude(36.3645845)
                .longitude(127.3412946)
                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                .placeName("카페 인터뷰")
                .memo("장소 메모")
                .category("카페")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/{scheduleId}/places", givenSchedule.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createRequest))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        List<SchedulePlace> foundSchedulePlaces = schedulePlaceRepository.findAllByScheduleId(givenSchedule.getId());
        assertThat(foundSchedulePlaces).hasSize(1);

        SchedulePlace foundSchedulePlace = foundSchedulePlaces.get(0);
        assertThat(foundSchedulePlace.getLatitude()).isEqualTo(createRequest.getLatitude());
        assertThat(foundSchedulePlace.getLongitude()).isEqualTo(createRequest.getLongitude());
        assertThat(foundSchedulePlace.getRoadAddress()).isEqualTo(createRequest.getRoadAddress());
        assertThat(foundSchedulePlace.getPlaceName()).isEqualTo(createRequest.getPlaceName());
        assertThat(foundSchedulePlace.getMemo()).isEqualTo(createRequest.getMemo());
        assertThat(foundSchedulePlace.getCategory()).isEqualTo(createRequest.getCategory());
    }

    @PlangoMockUser
    @Test
    void 타인의_일정에_장소를_추가하면_404에러가_반환된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        SchedulePlaceCreateRequestDto createRequest = SchedulePlaceCreateRequestDto.builder()
                .latitude(36.3645845)
                .longitude(127.3412946)
                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                .placeName("카페 인터뷰")
                .memo("장소 메모")
                .category("카페")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/{scheduleId}/places", givenSchedule.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createRequest))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 장소를_수정한다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .latitude(36.3645845)
                .longitude(127.3412946)
                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                .placeName("카페 인터뷰")
                .memo("첫번째 수업")
                .category("카페")
                .schedule(givenSchedule)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        SchedulePlaceEditRequestDto editRequest = SchedulePlaceEditRequestDto.builder()
                .memo("두번째 수업")
                .category("수업")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(put("/api/schedules/{scheduleId}/places/{placeId}", givenSchedule.getId(), givenSchedulePlace.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(editRequest))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<SchedulePlace> foundSchedulePlaces = schedulePlaceRepository.findAllByScheduleId(givenSchedule.getId());
        assertThat(foundSchedulePlaces).hasSize(1);

        SchedulePlace foundSchedulePlace = foundSchedulePlaces.get(0);
        assertThat(foundSchedulePlace.getMemo()).isEqualTo(editRequest.getMemo());
        assertThat(foundSchedulePlace.getCategory()).isEqualTo(editRequest.getCategory());
    }

    @PlangoMockUser
    @Test
    void 타인의_일정_장소를_수정하면_404에러가_반환된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .latitude(36.3645845)
                .longitude(127.3412946)
                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                .placeName("카페 인터뷰")
                .memo("첫번째 수업")
                .category("카페")
                .schedule(givenSchedule)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        SchedulePlaceEditRequestDto editRequest = SchedulePlaceEditRequestDto.builder()
                .memo("두번째 수업")
                .category("수업")
                .build();

        // when
        MockHttpServletResponse response = mockMvc.perform(put("/api/schedules/{scheduleId}/places/{placeId}", givenSchedule.getId(), givenSchedulePlace.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(editRequest))
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 일정의_장소를_삭제한다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .latitude(36.3645845)
                .longitude(127.3412946)
                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                .placeName("카페 인터뷰")
                .memo("첫번째 수업")
                .category("카페")
                .schedule(givenSchedule)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/{scheduleId}/places/{placeId}", givenSchedule.getId(), givenSchedulePlace.getId())
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        List<SchedulePlace> foundSchedulePlaces = schedulePlaceRepository.findAllByScheduleId(givenSchedule.getId());
        assertThat(foundSchedulePlaces).hasSize(0);
    }

    @PlangoMockUser
    @Test
    void 타인의_일정_장소를_삭제하면_404에러가_반환된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .latitude(36.3645845)
                .longitude(127.3412946)
                .roadAddress("대전광역시 유성구 한밭대로371번길 25-3")
                .placeName("카페 인터뷰")
                .memo("첫번째 수업")
                .category("카페")
                .schedule(givenSchedule)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/{scheduleId}/places/{placeId}", givenSchedule.getId(), givenSchedulePlace.getId())
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 장소를_confirm한다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(false)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/{scheduleId}/places/{placeId}/confirm", givenSchedule.getId(), givenSchedulePlace.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        SchedulePlace foundSchedulePlace = schedulePlaceRepository.findById(givenSchedulePlace.getId()).get();
        assertThat(foundSchedulePlace.isConfirmed()).isTrue();
    }

    @PlangoMockUser
    @Test
    void 타인_일정의_장소를_confirm하면_404에러가_반환된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(false)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/{scheduleId}/places/{placeId}/confirm", givenSchedule.getId(), givenSchedulePlace.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 장소를_deny한다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(true)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/{scheduleId}/places/{placeId}/confirm", givenSchedule.getId(), givenSchedulePlace.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        SchedulePlace foundSchedulePlace = schedulePlaceRepository.findById(givenSchedulePlace.getId()).get();
        assertThat(foundSchedulePlace.isConfirmed()).isFalse();
    }

    @PlangoMockUser
    @Test
    void 타인_일정의_장소를_deny하면_404에러가_반환된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(true)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/{scheduleId}/places/{placeId}/confirm", givenSchedule.getId(), givenSchedulePlace.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 장소_like_하면_새로운_SchedulePlaceLike가_생성된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(false)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/{scheduleId}/places/{placeId}/like", givenSchedule.getId(), givenSchedulePlace.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(schedulePlaceLikeRepository.findBySchedulePlaceIdAndMemberId(givenSchedulePlace.getId(), member.getId())).isPresent();
    }

    @PlangoMockUser
    @Test
    void 타인_일정의_장소_like_하면_404에러가_반환된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(false)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/schedules/{scheduleId}/places/{placeId}/like", givenSchedule.getId(), givenSchedulePlace.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 장소_dislike_하면_기존_SchedulePlaceLike가_삭제된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(false)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        SchedulePlaceLike givenSchedulePlaceLike = SchedulePlaceLike.builder()
                .member(member)
                .schedulePlace(givenSchedulePlace)
                .build();
        schedulePlaceLikeRepository.save(givenSchedulePlaceLike);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/{scheduleId}/places/{placeId}/like", givenSchedule.getId(), givenSchedulePlace.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(schedulePlaceLikeRepository.findBySchedulePlaceIdAndMemberId(givenSchedulePlace.getId(), member.getId())).isEmpty();
    }

    @PlangoMockUser
    @Test
    void 타인_일정의_장소_dislike_하면_404에러가_반환된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(false)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        SchedulePlaceLike givenSchedulePlaceLike = SchedulePlaceLike.builder()
                .member(member)
                .schedulePlace(givenSchedulePlace)
                .build();
        schedulePlaceLikeRepository.save(givenSchedulePlaceLike);

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/api/schedules/{scheduleId}/places/{placeId}/like", givenSchedule.getId(), givenSchedulePlace.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @PlangoMockUser
    @Test
    void 동일한_유저가_여러번_like를_해도_한번만_반영된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(false)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        // when
        MockHttpServletResponse response = null;
        int iterationCount = 5;
        for (int i = 0; i < iterationCount; i++) {
            response = mockMvc.perform(post("/api/schedules/{scheduleId}/places/{placeId}/like", givenSchedule.getId(), givenSchedulePlace.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse();
        }

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(schedulePlaceLikeRepository.countBySchedulePlaceIdAndMemberId(givenSchedulePlace.getId(), member.getId())).isEqualTo(1);
    }

    @PlangoMockUser
    @Test
    void 동일한_유저가_여러번_dislike릃_해도_한번만_반영된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);

        Schedule givenSchedule = Schedule.builder()
                .title("일정")
                .build();
        givenSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenSchedule);

        SchedulePlace givenSchedulePlace = SchedulePlace.builder()
                .placeName("장소")
                .schedule(givenSchedule)
                .confirmed(false)
                .build();
        schedulePlaceRepository.save(givenSchedulePlace);

        SchedulePlaceLike givenSchedulePlaceLike = SchedulePlaceLike.builder()
                .member(member)
                .schedulePlace(givenSchedulePlace)
                .build();
        schedulePlaceLikeRepository.save(givenSchedulePlaceLike);

        // when
        MockHttpServletResponse response = null;
        int iterationCount = 5;
        for (int i = 0; i < iterationCount; i++) {
            response = mockMvc.perform(delete("/api/schedules/{scheduleId}/places/{placeId}/like", givenSchedule.getId(), givenSchedulePlace.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse();
        }

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(schedulePlaceLikeRepository.countBySchedulePlaceIdAndMemberId(givenSchedulePlace.getId(), member.getId())).isEqualTo(0);
    }

    @PlangoMockUser
    @Test
    void 키워드_검색시_공백을_제거하고_장소_이름에서_검색된다() throws Exception {
        // given
        Member member = memberRepository.findAll().get(0);
        Member anotherMember = createAnotherMember();

        Schedule givenMemberSchedule = Schedule.builder()
                .title("현재 회원 일정")
                .content("일정 메모")
                .build();
        givenMemberSchedule.setSingleOwnerScheduleMember(member);
        scheduleRepository.save(givenMemberSchedule);

        Schedule givenAnotherMemberSchedule = Schedule.builder()
                .title("타 회원 일정")
                .content("일정 메모")
                .build();
        givenAnotherMemberSchedule.setSingleOwnerScheduleMember(anotherMember);
        scheduleRepository.save(givenAnotherMemberSchedule);

        List<SchedulePlace> givenMemberSchedulePlaces = List.of(
                SchedulePlace.builder()
                        .schedule(givenMemberSchedule)
                        .placeName("카페 A")
                        .build(),
                SchedulePlace.builder()
                        .schedule(givenMemberSchedule)
                        .placeName("카페 B")
                        .build()
        );
        schedulePlaceRepository.saveAll(givenMemberSchedulePlaces);

        SchedulePlace givenAnotherMemberSchedulePlace = SchedulePlace.builder()
                .schedule(givenAnotherMemberSchedule)
                .placeName("카페 C")
                .build();
        schedulePlaceRepository.save(givenAnotherMemberSchedulePlace);

        String query = "카 페";

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/schedulePlaces")
                .queryParam("query", query)
        ).andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        SchedulePlaceSearchResponseDto[] responseDtos = objectMapper.readValue(response.getContentAsString(UTF_8), SchedulePlaceSearchResponseDto[].class);
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(SchedulePlaceSearchResponseDto::getPlaceName)
                .containsExactlyInAnyOrder("카페 A", "카페 B");
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
