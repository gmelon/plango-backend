package dev.gmelon.plango.web.schedule;

import dev.gmelon.plango.auth.LoginMember;
import dev.gmelon.plango.auth.dto.SessionMember;
import dev.gmelon.plango.service.schedule.ScheduleService;
import dev.gmelon.plango.service.schedule.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/schedules")
@RestController
public class ScheduleController {

    private final ScheduleService scheduleService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@LoginMember SessionMember sessionMember,
                       @RequestBody @Valid ScheduleCreateRequestDto requestDto,
                       HttpServletResponse response) {
        Long scheduleId = scheduleService.create(sessionMember.getId(), requestDto);

        response.addHeader(HttpHeaders.LOCATION, "/api/v1/schedules/" + scheduleId);
    }

    @GetMapping("/{scheduleId}")
    public ScheduleResponseDto findById(@LoginMember SessionMember sessionMember,
                                        @PathVariable Long scheduleId) {
        return scheduleService.findById(sessionMember.getId(), scheduleId);
    }

    @GetMapping(params = "date")
    public List<ScheduleListResponseDto> findAllByDate(
            @LoginMember SessionMember sessionMember,
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("date") LocalDate requestDate,
            @RequestParam(defaultValue = "false") boolean noDiaryOnly) {
        return scheduleService.findAllByDate(sessionMember.getId(), requestDate, noDiaryOnly);
    }

    @GetMapping(params = "yearMonth")
    public List<ScheduleCountResponseDto> getCountOfDaysInMonth(
            @LoginMember SessionMember sessionMember,
            @DateTimeFormat(pattern = "yyyy-MM") @RequestParam("yearMonth") YearMonth requestYearMonth) {
        return scheduleService.getCountOfDaysInMonth(sessionMember.getId(), requestYearMonth);
    }

    @PatchMapping("/{scheduleId}")
    public void edit(@LoginMember SessionMember sessionMember,
                     @PathVariable Long scheduleId,
                     @RequestBody @Valid ScheduleEditRequestDto requestDto) {
        scheduleService.edit(sessionMember.getId(), scheduleId, requestDto);
    }

    @PatchMapping("/{scheduleId}/done")
    public void editDone(@LoginMember SessionMember sessionMember,
                         @PathVariable Long scheduleId,
                         @RequestBody @Valid ScheduleEditDoneRequestDto requestDto) {
        scheduleService.editDone(sessionMember.getId(), scheduleId, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{scheduleId}")
    public void delete(@LoginMember SessionMember sessionMember,
                       @PathVariable Long scheduleId) {
        scheduleService.delete(sessionMember.getId(), scheduleId);
    }
}
