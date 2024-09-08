package dev.gmelon.plango.domain.schedule.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.diary.repository.DiaryRepository;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.exception.NoSuchMemberException;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.schedule.dto.ScheduleCountResponseDto;
import dev.gmelon.plango.domain.schedule.dto.ScheduleCreateRequestDto;
import dev.gmelon.plango.domain.schedule.dto.ScheduleDateListResponseDto;
import dev.gmelon.plango.domain.schedule.dto.ScheduleEditDoneRequestDto;
import dev.gmelon.plango.domain.schedule.dto.ScheduleEditRequestDto;
import dev.gmelon.plango.domain.schedule.dto.ScheduleResponseDto;
import dev.gmelon.plango.domain.schedule.dto.ScheduleSearchResponseDto;
import dev.gmelon.plango.domain.schedule.dto.ScheduleTitlesResponseDto;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.schedule.entity.ScheduleEditor;
import dev.gmelon.plango.domain.schedule.entity.ScheduleMember;
import dev.gmelon.plango.domain.schedule.exception.NoOwnerOfScheduleException;
import dev.gmelon.plango.domain.schedule.exception.NoSuchScheduleException;
import dev.gmelon.plango.domain.schedule.exception.ScheduleAccessDeniedException;
import dev.gmelon.plango.domain.schedule.exception.ScheduleNotAcceptedException;
import dev.gmelon.plango.domain.schedule.exception.ScheduleOwnerParticipantDuplicateException;
import dev.gmelon.plango.domain.schedule.repository.ScheduleMemberRepository;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import dev.gmelon.plango.domain.schedule.repository.query.ScheduleQueryRepository;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleCountQueryDto;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleListQueryDto;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleQueryDto;
import dev.gmelon.plango.domain.schedule.repository.query.dto.ScheduleTitleQueryDto;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceCreateRequestDto;
import dev.gmelon.plango.domain.scheduleplace.entity.SchedulePlace;
import dev.gmelon.plango.global.infrastructure.s3.S3Repository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ScheduleService {

    private static final String WHITE_SPACE_REGEX = "\\s";

    private final ScheduleRepository scheduleRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final S3Repository s3Repository;
    private final ScheduleNotificationService scheduleNotificationService;
    private final ScheduleMemberRepository scheduleMemberRepository;

    @Transactional
    public Long create(Long memberId, ScheduleCreateRequestDto requestDto) {
        Schedule requestSchedule = requestDto.toEntity();
        Member owner = findMemberById(memberId);

        // TODO 리팩토링 하기
        List<ScheduleMember> participantScheduleMembers = mapToScheduleMembers(requestDto.getParticipantIds(),
                requestSchedule, owner);
        List<ScheduleMember> allScheduleMembers = addOwnerScheduleMember(participantScheduleMembers, owner,
                requestSchedule);
        requestSchedule.setScheduleMembers(allScheduleMembers);

        List<SchedulePlace> schedulePlaces = mapToSchedulePlaces(requestDto.getSchedulePlaces(), requestSchedule);
        requestSchedule.setSchedulePlaces(schedulePlaces);

        Schedule savedSchedule = scheduleRepository.save(requestSchedule);

        scheduleNotificationService.sendInvitedNotifications(savedSchedule, participantScheduleMembers);

        return savedSchedule.getId();
    }

    private List<ScheduleMember> mapToScheduleMembers(List<Long> participantIds, Schedule requestSchedule,
                                                      Member owner) {
        validateParticipantsAreNotOwner(participantIds, owner.getId());
        return participantIds.stream()
                .distinct()
                .map(participantId ->
                        ScheduleMember.createParticipant(findMemberById(participantId), requestSchedule))
                .collect(toList());
    }

    private void validateParticipantsAreNotOwner(List<Long> participantIds, Long ownerId) {
        participantIds.stream()
                .filter(participantId -> participantId.equals(ownerId))
                .findAny()
                .ifPresent((participantId) -> {
                    throw new ScheduleOwnerParticipantDuplicateException();
                });
    }

    private List<ScheduleMember> addOwnerScheduleMember(List<ScheduleMember> participantScheduleMembers, Member owner,
                                                        Schedule requestSchedule) {
        ArrayList<ScheduleMember> allScheduleMembers = new ArrayList<>(participantScheduleMembers);
        allScheduleMembers.add(ScheduleMember.createOwner(owner, requestSchedule));
        return allScheduleMembers;
    }

    private List<SchedulePlace> mapToSchedulePlaces(List<SchedulePlaceCreateRequestDto> schedulePlaces,
                                                    Schedule requestSchedule) {
        return schedulePlaces.stream()
                .map(schedulePlaceCreateRequestDto -> schedulePlaceCreateRequestDto.toEntity(requestSchedule))
                .collect(toList());
    }

    public ScheduleResponseDto findById(Long memberId, Long scheduleId) {
        validateMember(memberId, scheduleId);

        ScheduleQueryDto scheduleQueryDto = scheduleQueryRepository.findOneById(scheduleId, memberId);
        return ScheduleResponseDto.from(scheduleQueryDto);
    }

    private void validateMember(Long memberId, Long scheduleId) {
        boolean isMemberNotExists = scheduleMemberRepository.findByMemberIdAndScheduleId(memberId, scheduleId)
                .isEmpty();
        if (isMemberNotExists) {
            throw new ScheduleAccessDeniedException();
        }
    }

    public List<ScheduleDateListResponseDto> findAllByDate(Long memberId, LocalDate requestDate, boolean noDiaryOnly) {
        List<ScheduleListQueryDto> schedules = scheduleQueryRepository.findAllByMemberIdAndDate(memberId, requestDate);
        if (noDiaryOnly) {
            schedules = filterDoNotHaveDiaryOnly(memberId, schedules);
        }

        return schedules.stream()
                .map(ScheduleDateListResponseDto::from)
                .collect(toList());
    }

    private List<ScheduleListQueryDto> filterDoNotHaveDiaryOnly(Long memberId, List<ScheduleListQueryDto> schedules) {
        return schedules.stream()
                .filter(schedule -> !isDiaryPresent(memberId, schedule.getId()))
                .filter(ScheduleListQueryDto::getIsAccepted)
                .collect(toList());
    }

    private boolean isDiaryPresent(Long memberId, Long scheduleId) {
        return diaryRepository.findByMemberIdAndScheduleId(memberId, scheduleId).isPresent();
    }

    @Transactional
    public void edit(Long editorMemberId, Long scheduleId, ScheduleEditRequestDto requestDto) {
        Member editorMember = findMemberById(editorMemberId);
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);

        validateMember(schedule, editorMember);
        validateAccepted(schedule, editorMember);

        ScheduleEditor scheduleEditor = requestDto.toEditor();
        schedule.edit(scheduleEditor);

        scheduleNotificationService.sendEditedNotifications(schedule, editorMember);
    }

    @Transactional
    public void editDone(Long memberId, Long scheduleId, ScheduleEditDoneRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);

        validateMember(schedule, member);
        validateAccepted(schedule, member);

        schedule.changeDone(requestDto.getIsDone());
    }

    @Transactional
    public void delete(Long memberId, Long scheduleId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleByIdWithMembers(scheduleId);

        validateMember(schedule, member);
        validateOwner(schedule, member);

        scheduleNotificationService.sendDeletedNotification(schedule, memberId);

        deleteAllDiaryImages(schedule);
        scheduleRepository.delete(schedule);
    }

    private void deleteAllDiaryImages(Schedule schedule) {
        List<String> diaryImageUrls = findDiaryImageUrlsByScheduleId(schedule.getId());
        s3Repository.deleteAll(diaryImageUrls);
    }

    private List<String> findDiaryImageUrlsByScheduleId(Long scheduleId) {
        return diaryRepository.findAllByScheduleId(scheduleId).stream()
                .flatMap(diary -> diary.getDiaryImageUrls().stream())
                .collect(toList());
    }

    public List<ScheduleCountResponseDto> getCountByYearMonth(Long memberId, YearMonth requestMonth) {
        LocalDate startDate = requestMonth.atDay(1);
        LocalDate endDate = requestMonth.atEndOfMonth();

        List<ScheduleCountQueryDto> countQueryDtos = scheduleQueryRepository.countBetweenDatesByMemberId(memberId,
                startDate, endDate);
        return countQueryDtos.stream()
                .map(ScheduleCountResponseDto::from)
                .collect(toList());
    }

    public List<ScheduleTitlesResponseDto> getTitlesByYearMonth(Long memberId, YearMonth requestMonth) {
        LocalDate startDate = requestMonth.atDay(1);
        LocalDate endDate = requestMonth.atEndOfMonth();

        List<ScheduleTitleQueryDto> titleQueryDtos = scheduleQueryRepository.titlesBetweenDatesByMemberId(
                memberId, startDate, endDate);
        return groupingByDate(titleQueryDtos);
    }

    private List<ScheduleTitlesResponseDto> groupingByDate(List<ScheduleTitleQueryDto> titleQueryDtos) {
        Map<LocalDate, List<String>> titlesByDate = titleQueryDtos.stream()
                .collect(groupingBy(ScheduleTitleQueryDto::getDate, mapping(ScheduleTitleQueryDto::getTitle, toList())));

        return titlesByDate.keySet().stream()
                .map(key -> ScheduleTitlesResponseDto.builder()
                        .date(key)
                        .titles(titlesByDate.get(key))
                        .build()
                )
                .collect(toList());
    }

    public List<ScheduleSearchResponseDto> search(Long memberId, String query, int page) {
        List<Schedule> results = scheduleRepository.search(memberId, trim(query), page);
        return results.stream()
                .map(ScheduleSearchResponseDto::from)
                .collect(toList());
    }

    private String trim(String string) {
        return string.replaceAll(WHITE_SPACE_REGEX, "");
    }

    private void validateMember(Schedule schedule, Member member) {
        if (!schedule.isMember(member.getId())) {
            throw new ScheduleAccessDeniedException();
        }
    }

    private void validateAccepted(Schedule schedule, Member member) {
        if (!schedule.isAccepted(member.getId())) {
            throw new ScheduleNotAcceptedException();
        }
    }

    private void validateOwner(Schedule schedule, Member member) {
        if (!schedule.isOwner(member.getId())) {
            throw new NoOwnerOfScheduleException();
        }
    }

    private Schedule findScheduleByIdWithMembers(Long scheduleId) {
        return scheduleRepository.findByIdWithMembers(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
