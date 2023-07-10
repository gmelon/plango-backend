package dev.gmelon.plango.web.diary;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.diary.DiaryService;
import dev.gmelon.plango.service.diary.dto.DiaryCreateRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryEditRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryListResponseDto;
import dev.gmelon.plango.service.diary.dto.DiaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class DiaryController {

    private final DiaryService diaryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/schedules/{scheduleId}/diary")
    public void create(@LoginMember Long memberId,
                       @PathVariable Long scheduleId,
                       @RequestBody @Valid DiaryCreateRequestDto requestDto,
                       HttpServletResponse response) {
        Long diaryId = diaryService.create(memberId, scheduleId, requestDto);

        response.addHeader(HttpHeaders.LOCATION, "/api/diaries/" + diaryId);
    }

    @GetMapping("/diaries/{diaryId}")
    public DiaryResponseDto findById(@LoginMember Long memberId, @PathVariable Long diaryId) {
        return diaryService.findById(memberId, diaryId);
    }

    @GetMapping("/schedules/{scheduleId}/diary")
    public DiaryResponseDto findByScheduleId(@LoginMember Long memberId,
                                             @PathVariable Long scheduleId) {
        return diaryService.findByScheduleId(memberId, scheduleId);
    }

    @GetMapping(path = "/diaries", params = "date")
    public List<DiaryListResponseDto> findAllByDate(
            @LoginMember Long memberId,
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("date") LocalDate requestDate) {
        return diaryService.findAllByDate(memberId, requestDate);
    }

    @PutMapping("/diaries/{diaryId}")
    public void edit(@LoginMember Long memberId,
                     @PathVariable Long diaryId,
                     @RequestBody @Valid DiaryEditRequestDto requestDto) {
        diaryService.edit(memberId, diaryId, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/diaries/{diaryId}")
    public void delete(@LoginMember Long memberId,
                       @PathVariable Long diaryId) {
        diaryService.delete(memberId, diaryId);
    }
}
