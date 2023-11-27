package dev.gmelon.plango.web.diary;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.diary.DiaryService;
import dev.gmelon.plango.service.diary.dto.DiaryCreateRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryDateListResponseDto;
import dev.gmelon.plango.service.diary.dto.DiaryEditRequestDto;
import dev.gmelon.plango.service.diary.dto.DiaryListResponseDto;
import dev.gmelon.plango.service.diary.dto.DiaryResponseDto;
import dev.gmelon.plango.service.diary.dto.DiarySearchResponseDto;
import dev.gmelon.plango.web.diary.validator.DiaryCreateRequestValidator;
import dev.gmelon.plango.web.diary.validator.DiaryEditRequestValidator;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryCreateRequestValidator diaryCreateRequestValidator;
    private final DiaryEditRequestValidator diaryEditRequestValidator;

    @InitBinder("diaryCreateRequestDto")
    public void diaryCreateRequestDtoInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(diaryCreateRequestValidator);
    }

    @InitBinder("diaryEditRequestDto")
    public void diaryEditRequestDtoInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(diaryEditRequestValidator);
    }

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
    public List<DiaryDateListResponseDto> findAllByDate(
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

    @GetMapping("/diaries")
    public List<DiaryListResponseDto> findAll(@LoginMember Long memberId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam int size) {
        return diaryService.findAll(memberId, page, size);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/diaries/{diaryId}")
    public void delete(@LoginMember Long memberId,
                       @PathVariable Long diaryId) {
        diaryService.delete(memberId, diaryId);
    }

    @GetMapping(value = "/diaries", params = "query")
    public List<DiarySearchResponseDto> search(
            @LoginMember Long memberId,
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page) {
        return diaryService.search(memberId, query, page);
    }

}
