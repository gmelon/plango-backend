package dev.gmelon.plango.service.diary;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.UnauthorizedException;
import dev.gmelon.plango.infrastructure.s3.S3Repository;
import dev.gmelon.plango.service.diary.dto.DiaryCreateRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryEditRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryListResponseDto;
import dev.gmelon.plango.service.diary.dto.DiaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        LocalDateTime startDateTime = LocalDateTime.of(requestDate, LocalTime.of(0, 0, 0));
        LocalDateTime endDateTime = LocalDateTime.of(requestDate, LocalTime.of(23, 59, 59));

        List<Schedule> schedules = scheduleRepository.findByMemberIdAndStartTimeBetweenAndDiaryNotNullOrderByStartTimeAscEndTimeAsc(memberId, startDateTime, endDateTime);

        // TODO fetch join
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

        diary.edit(requestDto.toDiaryEditor());
        deleteImageIfChanged(prevDiaryImageUrl, requestDto);
    }

    private void deleteImageIfChanged(String prevDiaryImageUrl, DiaryEditRequestDto requestDto) {
        if (!prevDiaryImageUrl.equals(requestDto.getImageUrl())) {
            s3Repository.delete(prevDiaryImageUrl);
        }
    }

    @Transactional
    public void delete(Long memberId, Long diaryId) {
        Member member = findMemberById(memberId);
        Schedule schedule = findScheduleByDiaryId(diaryId);

        validateMember(schedule, member);

        String diaryImageUrl = schedule.getDiary().getImageUrl();

        schedule.deleteDiary();
        s3Repository.delete(diaryImageUrl);
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다."));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }
}
