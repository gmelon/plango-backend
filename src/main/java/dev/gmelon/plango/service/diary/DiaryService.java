package dev.gmelon.plango.service.diary;

import dev.gmelon.plango.domain.diary.Diary;
import dev.gmelon.plango.domain.diary.DiaryRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.schedule.Schedule;
import dev.gmelon.plango.domain.schedule.ScheduleRepository;
import dev.gmelon.plango.exception.UnauthorizedException;
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
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

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
        Schedule schedule = scheduleRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다."));

        validateMember(schedule, member);

        return DiaryResponseDto.of(schedule.getDiary());
    }

    public List<DiaryListResponseDto> findAllByDate(Long memberId, LocalDate requestDate) {
        LocalDateTime startDateTime = LocalDateTime.of(requestDate, LocalTime.of(0, 0, 0));
        LocalDateTime endDateTime = LocalDateTime.of(requestDate, LocalTime.of(23, 59, 59));

        List<Schedule> schedules = scheduleRepository.findByMemberIdAndStartTimeBetweenOrderByStartTimeAsc(memberId, startDateTime, endDateTime);

        // TODO fetch join?
        return schedules.stream()
                .map(Schedule::getDiary)
                .filter(Objects::nonNull)
                .map(DiaryListResponseDto::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void edit(Long memberId, Long diaryId, DiaryEditRequestDto requestDto) {
        Member member = findMemberById(memberId);
        Schedule schedule = scheduleRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다."));

        validateMember(schedule, member);

        Diary diary = schedule.getDiary();
        diary.edit(requestDto.toDiaryEditor());
    }

    @Transactional
    public void delete(Long memberId, Long diaryId) {
        Member member = findMemberById(memberId);
        Schedule schedule = scheduleRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다."));

        validateMember(schedule, member);

        schedule.deleteDiary();
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

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }
}
