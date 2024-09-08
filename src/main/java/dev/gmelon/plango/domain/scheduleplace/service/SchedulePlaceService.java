package dev.gmelon.plango.domain.scheduleplace.service;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.exception.NoSuchMemberException;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.schedule.entity.ScheduleMember;
import dev.gmelon.plango.domain.schedule.exception.NoSuchScheduleException;
import dev.gmelon.plango.domain.schedule.exception.ScheduleAccessDeniedException;
import dev.gmelon.plango.domain.schedule.exception.ScheduleNotAcceptedException;
import dev.gmelon.plango.domain.schedule.repository.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceCreateRequestDto;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceEditRequestDto;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceSearchResponseDto;
import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlace;
import dev.gmelon.plango.domain.scheduleplace.exception.NoSuchSchedulePlaceException;
import dev.gmelon.plango.domain.scheduleplace.repository.SchedulePlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SchedulePlaceService {

    private static final String WHITE_SPACE_REGEX = "\\s";

    private final SchedulePlaceRepository schedulePlaceRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final ScheduleMemberRepository scheduleMemberRepository;

    @Transactional
    public void add(Long memberId, Long scheduleId, SchedulePlaceCreateRequestDto requestDto) {
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);
        validateScheduleMember(schedule, memberId);

        schedule.addSchedulePlace(requestDto.toEntity(schedule));
    }

    @Transactional
    public void edit(Long memberId, Long scheduleId, Long placeId, SchedulePlaceEditRequestDto requestDto) {
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);
        validateScheduleMember(schedule, memberId);

        SchedulePlace schedulePlace = findSchedulePlaceById(placeId);
        schedulePlace.edit(requestDto.toEditor());
    }

    @Transactional
    public void remove(Long memberId, Long scheduleId, Long placeId) {
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);
        validateScheduleMember(schedule, memberId);

        SchedulePlace schedulePlace = findSchedulePlaceById(placeId);
        schedule.removeSchedulePlace(schedulePlace);
    }

    @Transactional
    public void confirm(Long memberId, Long scheduleId, Long placeId) {
        validateScheduleMember(scheduleId, memberId);

        SchedulePlace schedulePlace = findSchedulePlaceById(placeId);
        schedulePlace.confirm();
    }

    @Transactional
    public void deny(Long memberId, Long scheduleId, Long placeId) {
        validateScheduleMember(scheduleId, memberId);

        SchedulePlace schedulePlace = findSchedulePlaceById(placeId);
        schedulePlace.deny();
    }

    @Transactional
    public void like(Long memberId, Long scheduleId, Long placeId) {
        validateScheduleMember(scheduleId, memberId);

        SchedulePlace schedulePlace = findSchedulePlaceById(placeId);
        schedulePlace.like(findMemberById(memberId));
    }

    @Transactional
    public void dislike(Long memberId, Long scheduleId, Long placeId) {
        validateScheduleMember(scheduleId, memberId);

        SchedulePlace schedulePlace = findSchedulePlaceById(placeId);
        schedulePlace.dislike(memberId);
    }

    public List<SchedulePlaceSearchResponseDto> search(Long memberId, String query, int page) {
        List<SchedulePlace> results = schedulePlaceRepository.search(memberId, trim(query), page);
        return results.stream()
                .map(SchedulePlaceSearchResponseDto::from)
                .collect(toList());
    }

    private String trim(String string) {
        return string.replaceAll(WHITE_SPACE_REGEX, "");
    }

    private void validateScheduleMember(Schedule schedule, Long memberId) {
        if (!schedule.isMember(memberId)) {
            throw new ScheduleAccessDeniedException();
        }
        if (!schedule.isAccepted(memberId)) {
            throw new ScheduleNotAcceptedException();
        }
    }

    private void validateScheduleMember(Long scheduleId, Long memberId) {
        ScheduleMember scheduleMember = findScheduleMemberByMemberIdAndScheduleId(memberId, scheduleId);
        if (!scheduleMember.isAccepted()) {
            throw new ScheduleNotAcceptedException();
        }
    }

    private ScheduleMember findScheduleMemberByMemberIdAndScheduleId(Long memberId, Long scheduleId) {
        return scheduleMemberRepository.findByMemberIdAndScheduleId(memberId, scheduleId)
                .orElseThrow(ScheduleAccessDeniedException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }

    private Schedule findScheduleByIdWithMembers(Long scheduleId) {
        return scheduleRepository.findByIdWithMembers(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private SchedulePlace findSchedulePlaceById(Long placeId) {
        return schedulePlaceRepository.findById(placeId)
                .orElseThrow(NoSuchSchedulePlaceException::new);
    }
}
