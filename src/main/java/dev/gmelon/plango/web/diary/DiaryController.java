package dev.gmelon.plango.web.diary;

import dev.gmelon.plango.auth.LoginMember;
import dev.gmelon.plango.auth.dto.SessionMember;
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
@RequestMapping("/api/v1")
@RestController
public class DiaryController {

    private final DiaryService diaryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/schedules/{scheduleId}/diary")
    public void create(@LoginMember SessionMember sessionMember,
                       @PathVariable Long scheduleId,
                       @RequestBody @Valid DiaryCreateRequestDto requestDto,
                       HttpServletResponse response) {
        Long diaryId = diaryService.create(sessionMember.getId(), scheduleId, requestDto);

        response.addHeader(HttpHeaders.LOCATION, "/api/v1/diaries/" + diaryId);
    }

    @GetMapping("/diaries/{diaryId}")
    public DiaryResponseDto findById(@LoginMember SessionMember sessionMember, @PathVariable Long diaryId) {
        return diaryService.findById(sessionMember.getId(), diaryId);
    }

    @GetMapping(path = "/diaries", params = "date")
    public List<DiaryListResponseDto> findAllByDate(
            @LoginMember SessionMember sessionMember,
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("date") LocalDate requestDate) {
        return diaryService.findAllByDate(sessionMember.getId(), requestDate);
    }

    @PutMapping("/diaries/{diaryId}")
    public void edit(@LoginMember SessionMember sessionMember,
                     @PathVariable Long diaryId,
                     @RequestBody @Valid DiaryEditRequestDto requestDto) {
        diaryService.edit(sessionMember.getId(), diaryId, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/diaries/{diaryId}")
    public void delete(@LoginMember SessionMember sessionMember,
                       @PathVariable Long diaryId) {
        diaryService.delete(sessionMember.getId(), diaryId);
    }
}
