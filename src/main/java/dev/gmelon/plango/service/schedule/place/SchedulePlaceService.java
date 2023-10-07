package dev.gmelon.plango.service.schedule.place;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleMember;
import dev.gmelon.plango.domain.schedule.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.place.SchedulePlace;
import dev.gmelon.plango.domain.schedule.place.SchedulePlaceLikeRepository;
import dev.gmelon.plango.domain.schedule.place.SchedulePlaceRepository;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.schedule.NoSuchScheduleException;
import dev.gmelon.plango.exception.schedule.ScheduleAccessDeniedException;
import dev.gmelon.plango.exception.schedule.ScheduleNotAcceptedException;
import dev.gmelon.plango.exception.schedule.place.NoSuchSchedulePlaceException;
import dev.gmelon.plango.service.schedule.place.dto.SchedulePlaceCreateRequestDto;
import dev.gmelon.plango.service.schedule.place.dto.SchedulePlaceEditRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SchedulePlaceService {

    private final SchedulePlaceRepository schedulePlaceRepository;
    private final SchedulePlaceLikeRepository schedulePlaceLikeRepository;
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
