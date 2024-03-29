package dev.gmelon.plango.domain.diary.service;

import static java.util.stream.Collectors.toList;

import dev.gmelon.plango.domain.diary.dto.DiaryCreateRequestDto;
import dev.gmelon.plango.domain.diary.dto.DiaryDateListResponseDto;
import dev.gmelon.plango.domain.diary.dto.DiaryEditRequestDto;
import dev.gmelon.plango.domain.diary.dto.DiaryListResponseDto;
import dev.gmelon.plango.domain.diary.dto.DiaryResponseDto;
import dev.gmelon.plango.domain.diary.dto.DiarySearchResponseDto;
import dev.gmelon.plango.domain.diary.entity.Diary;
import dev.gmelon.plango.domain.diary.exception.DiaryAccessDeniedException;
import dev.gmelon.plango.domain.diary.exception.DuplicateDiaryException;
import dev.gmelon.plango.domain.diary.exception.NoSuchDiaryException;
import dev.gmelon.plango.domain.diary.repository.DiaryRepository;
import dev.gmelon.plango.domain.member.entity.Member;
import dev.gmelon.plango.domain.member.exception.NoSuchMemberException;
import dev.gmelon.plango.domain.member.repository.MemberRepository;
import dev.gmelon.plango.domain.schedule.entity.Schedule;
import dev.gmelon.plango.domain.schedule.exception.NoSuchScheduleException;
import dev.gmelon.plango.domain.schedule.exception.ScheduleAccessDeniedException;
import dev.gmelon.plango.domain.schedule.exception.ScheduleNotAcceptedException;
import dev.gmelon.plango.domain.schedule.repository.ScheduleRepository;
import dev.gmelon.plango.global.infrastructure.s3.S3Repository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DiaryService {

    private static final String WHITE_SPACE_REGEX = "\\s";

    private final DiaryRepository diaryRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final S3Repository s3Repository;

    @Transactional
    public Long create(Long memberId, Long scheduleId, DiaryCreateRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleAccessPermission(memberId, schedule);
        validateScheduleIsAccepted(memberId, schedule);
        validateDiaryNotExist(memberId, schedule.getId());

        Diary diary = requestDto.toEntity(member, schedule);
        diaryRepository.save(diary);

        return diary.getId();
    }

    private void validateScheduleAccessPermission(Long memberid, Schedule schedule) {
        if (!schedule.isMember(memberid)) {
            throw new ScheduleAccessDeniedException();
        }
    }

    private void validateScheduleIsAccepted(Long memberId, Schedule schedule) {
        if (!schedule.isAccepted(memberId)) {
            throw new ScheduleNotAcceptedException();
        }
    }

    private void validateDiaryNotExist(Long memberId, Long scheduleId) {
        boolean isPresent = diaryRepository.findByMemberIdAndScheduleId(memberId, scheduleId).isPresent();
        if (isPresent) {
            throw new DuplicateDiaryException();
        }
    }

    public DiaryResponseDto findById(Long memberId, Long diaryId) {
        validateDiaryAccessPermission(memberId, diaryId);

        Diary diary = findDiaryByIdWithSchedule(diaryId);
        return DiaryResponseDto.from(diary, diary.getSchedule());
    }

    private Diary findDiaryByIdWithSchedule(Long diaryId) {
        return diaryRepository.findByIdWithSchedule(diaryId)
                .orElseThrow(NoSuchDiaryException::new);
    }

    public DiaryResponseDto findByScheduleId(Long memberId, Long scheduleId) {
        Diary diary = findDiaryByMemberIdAndScheduleId(memberId, scheduleId);
        return DiaryResponseDto.from(diary, diary.getSchedule());
    }

    private Diary findDiaryByMemberIdAndScheduleId(Long memberId, Long scheduleId) {
        return diaryRepository.findByMemberIdAndScheduleId(memberId, scheduleId)
                .orElseThrow(NoSuchDiaryException::new);
    }

    public List<DiaryDateListResponseDto> findAllByDate(Long memberId, LocalDate requestDate) {
        List<Diary> diaries = diaryRepository.findAllByMemberIdAndDate(memberId, requestDate);

        return diaries.stream()
                .map(diary -> DiaryDateListResponseDto.from(diary, diary.getSchedule()))
                .collect(toList());
    }

    @Transactional
    public void edit(Long memberId, Long diaryId, DiaryEditRequestDto requestDto) {
        validateDiaryAccessPermission(memberId, diaryId);

        Diary diary = findDiaryById(diaryId);
        diary.edit(requestDto.toEditor());
    }

    public List<DiaryListResponseDto> findAll(Long memberId, int page, int size) {
        return diaryRepository.findAllByMemberId(memberId, page, size).stream()
                .map(DiaryListResponseDto::from)
                .collect(toList());
    }

    @Transactional
    public void delete(Long memberId, Long diaryId) {
        validateDiaryAccessPermission(memberId, diaryId);

        Diary diary = findDiaryById(diaryId);
        deleteDiaryImages(diary);

        diaryRepository.delete(diary);
    }

    public List<DiarySearchResponseDto> search(Long memberId, String query, int page) {
        List<Diary> results = diaryRepository.search(memberId, trim(query), page);
        return results.stream()
                .map(DiarySearchResponseDto::from)
                .collect(toList());
    }

    private String trim(String string) {
        return string.replaceAll(WHITE_SPACE_REGEX, "");
    }

    private void deleteDiaryImages(Diary diary) {
        List<String> diaryImageUrls = diary.getDiaryImageUrls();
        s3Repository.deleteAll(diaryImageUrls);
    }

    private void validateDiaryAccessPermission(Long memberId, Long diaryId) {
        Diary diary = findDiaryById(diaryId);

        if (!diary.memberId().equals(memberId)) {
            throw new DiaryAccessDeniedException();
        }
    }

    private Diary findDiaryById(Long diaryId) {
        return diaryRepository.findById(diaryId)
                .orElseThrow(NoSuchDiaryException::new);
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findByIdWithMembers(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
