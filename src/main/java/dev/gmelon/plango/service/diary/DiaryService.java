package dev.gmelon.plango.service.diary;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.diary.NoSuchDiaryException;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.schedule.ScheduleAccessDeniedException;
import dev.gmelon.plango.exception.schedule.NoSuchScheduleException;
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
import java.util.stream.Collectors;

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

        validateMember(schedule, member);

        Diary diary = requestDto.toEntity();
        schedule.addDiary(diary);
        diaryRepository.save(diary);

        return diary.getId();
    }

    public DiaryResponseDto findById(Long memberId, Long diaryId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleByDiaryId(diaryId);

        validateMember(schedule, member);

        return DiaryResponseDto.from(schedule);
    }

    public DiaryResponseDto findByScheduleId(Long memberId, Long scheduleId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleById(scheduleId);

        validateMember(schedule, member);

        return DiaryResponseDto.from(schedule);
    }

    public List<DiaryListResponseDto> findAllByDate(Long memberId, LocalDate requestDate) {
        List<Schedule> schedules = scheduleRepository.findByMemberIdAndDateAndDiaryNotNull(memberId, requestDate);

        return schedules.stream()
                .map(DiaryListResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void edit(Long memberId, Long diaryId, DiaryEditRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleByDiaryId(diaryId);

        validateMember(schedule, member);

        Diary diary = schedule.getDiary();
        diary.edit(requestDto.toEditor());
    }

    @Transactional
    public void delete(Long memberId, Long diaryId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleByDiaryId(diaryId);

        validateMember(schedule, member);

        deleteDiaryImages(schedule.getDiary());
        schedule.deleteDiary();
    }

    private void deleteDiaryImages(Diary diary) {
        List<String> diaryImageUrls = diary.getDiaryImageUrls();
        s3Repository.deleteAll(diaryImageUrls);
    }

    private void validateMember(Schedule schedule, Member member) {
        if (!schedule.getMember().equals(member)) {
            throw new ScheduleAccessDeniedException();
        }
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(NoSuchScheduleException::new);
    }

    private Schedule findScheduleByDiaryId(Long diaryId) {
        return scheduleRepository.findByDiaryId(diaryId)
                .orElseThrow(NoSuchDiaryException::new);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }
}
