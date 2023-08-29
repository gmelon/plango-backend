package dev.gmelon.plango.service.diary;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.diary.DiaryAccessDeniedException;
import dev.gmelon.plango.exception.diary.DuplicateDiaryException;
import dev.gmelon.plango.exception.diary.NoSuchDiaryException;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.schedule.NoSuchScheduleException;
import dev.gmelon.plango.exception.schedule.ScheduleAccessDeniedException;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.diary.dto.DiaryCreateRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryEditRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryListResponseDto;
import dev.gmelon.plango.service.diary.dto.DiaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final S3Repository s3Repository;

    @Transactional
    public Long create(Long memberId, Long scheduleId, DiaryCreateRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleAccessPermission(member, schedule);
        validateDiaryExistence(memberId, schedule.getId());

        Diary diary = requestDto.toEntity(member, schedule);
        diaryRepository.save(diary);

        return diary.getId();
    }

    private void validateScheduleAccessPermission(Member member, Schedule schedule) {
        // TODO schedule에 member 일대다 구성 후 조건 변경 필요
        if (!schedule.getMember().equals(member)) {
            throw new ScheduleAccessDeniedException();
        }
    }

    private void validateDiaryExistence(Long memberId, Long scheduleId) {
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

    public List<DiaryListResponseDto> findAllByDate(Long memberId, LocalDate requestDate) {
        List<Diary> diaries = diaryRepository.findAllByMemberIdAndDate(memberId, requestDate);

        return diaries.stream()
                .map(diary -> DiaryListResponseDto.from(diary, diary.getSchedule()))
                .collect(toList());
    }

    @Transactional
    public void edit(Long memberId, Long diaryId, DiaryEditRequestDto requestDto) {
        validateDiaryAccessPermission(memberId, diaryId);

        Diary diary = findDiaryById(diaryId);
        diary.edit(requestDto.toEditor());
    }

    @Transactional
    public void delete(Long memberId, Long diaryId) {
        validateDiaryAccessPermission(memberId, diaryId);

        Diary diary = findDiaryById(diaryId);
        deleteDiaryImages(diary);

        diaryRepository.delete(diary);
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
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
