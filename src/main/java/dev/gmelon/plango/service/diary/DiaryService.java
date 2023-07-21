package dev.gmelon.plango.service.diary;

import dev.gmelon.plango.config.auth.exception.UnauthorizedException;
import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
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
        List<Schedule> schedules = scheduleRepository.findByMemberIdAndDateAndDiaryNotNullOrderByStartTimeAndEndTimeAsc(memberId, requestDate);

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
        String prevDiaryImageUrl = diary.getImageUrl();

        diary.edit(requestDto.toEditor());
        deletePrevImageIfChanged(prevDiaryImageUrl, requestDto);
    }

    private void deletePrevImageIfChanged(String prevDiaryImageUrl, DiaryEditRequestDto requestDto) {
        if (prevDiaryImageUrl == null) {
            return;
        }
        if (isImageChangedOrDeleted(prevDiaryImageUrl, requestDto)) {
            s3Repository.delete(prevDiaryImageUrl);
        }
    }

    private boolean isImageChangedOrDeleted(String prevDiaryImageUrl, DiaryEditRequestDto requestDto) {
        return requestDto.getImageUrl() == null || !prevDiaryImageUrl.equals(requestDto.getImageUrl());
    }

    @Transactional
    public void delete(Long memberId, Long diaryId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleByDiaryId(diaryId);

        validateMember(schedule, member);

        String diaryImageUrl = schedule.getDiary().getImageUrl();

        schedule.deleteDiary();
        deleteImageIfExists(diaryImageUrl);
    }

    private void deleteImageIfExists(String diaryImageUrl) {
        if (diaryImageUrl != null) {
            s3Repository.delete(diaryImageUrl);
        }
    }

    private void validateMember(Schedule schedule, Member member) {
        if (!schedule.getMember().equals(member)) {
            throw new UnauthorizedException();
        }
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계획입니다."));
    }

    private Schedule findScheduleByDiaryId(Long diaryId) {
        return scheduleRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계획입니다."));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }
}
