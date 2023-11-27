package dev.gmelon.plango.web.schedule;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.schedule.ScheduleService;
import dev.gmelon.plango.service.schedule.dto.*;
import dev.gmelon.plango.web.schedule.validator.ScheduleCreateRequestValidator;
import dev.gmelon.plango.web.schedule.validator.ScheduleEditRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/schedules")
@RestController
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleCreateRequestValidator scheduleCreateRequestValidator;
    private final ScheduleEditRequestValidator scheduleEditRequestValidator;

    @InitBinder("scheduleCreateRequestDto")
    public void scheduleCreateRequestDtoInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(scheduleCreateRequestValidator);
    }

    @InitBinder("scheduleEditRequestDto")
    public void scheduleEditRequestDtoInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(scheduleEditRequestValidator);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@LoginMember Long memberId,
                       @RequestBody @Valid ScheduleCreateRequestDto requestDto,
                       HttpServletResponse response) {
        Long scheduleId = scheduleService.create(memberId, requestDto);

        response.addHeader(HttpHeaders.LOCATION, "/api/schedules/" + scheduleId);
    }

    @GetMapping("/{scheduleId}")
    public ScheduleResponseDto findById(@LoginMember Long memberId,
                                        @PathVariable Long scheduleId) {
        return scheduleService.findById(memberId, scheduleId);
    }

    @GetMapping(params = "date")
    public List<ScheduleDateListResponseDto> findAllByDate(
            @LoginMember Long memberId,
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("date") LocalDate requestDate,
            @RequestParam(defaultValue = "false") boolean noDiaryOnly) {
        return scheduleService.findAllByDate(memberId, requestDate, noDiaryOnly);
    }

    @GetMapping(params = "yearMonth")
    public List<ScheduleCountResponseDto> getCountByDays(
            @LoginMember Long memberId,
            @DateTimeFormat(pattern = "yyyy-MM") @RequestParam("yearMonth") YearMonth requestYearMonth) {
        return scheduleService.getCountByDays(memberId, requestYearMonth);
    }

    @PatchMapping("/{scheduleId}")
    public void edit(@LoginMember Long memberId,
                     @PathVariable Long scheduleId,
                     @RequestBody @Valid ScheduleEditRequestDto requestDto) {
        scheduleService.edit(memberId, scheduleId, requestDto);
    }

    @PatchMapping("/{scheduleId}/done")
    public void editDone(@LoginMember Long memberId,
                         @PathVariable Long scheduleId,
                         @RequestBody @Valid ScheduleEditDoneRequestDto requestDto) {
        scheduleService.editDone(memberId, scheduleId, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{scheduleId}")
    public void delete(@LoginMember Long memberId,
                       @PathVariable Long scheduleId) {
        scheduleService.delete(memberId, scheduleId);
    }

    @GetMapping(params = "query")
    public List<ScheduleSearchResponseDto> search(
            @LoginMember Long memberId,
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page) {
        return scheduleService.search(memberId, query, page);
    }

}
