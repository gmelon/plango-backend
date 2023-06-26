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

    // 개발 순서
    // 컨트롤러 & DTO 개발 -> 서비스 컴파일 오류 해결 -> 컨트롤러 테스트 -> 서비스 작성 -> 서비스 테스트

    private final ScheduleService scheduleService;

    // 스케쥴 생성
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@LoginMember SessionMember sessionMember,
                       @RequestBody @Valid ScheduleCreateRequestDto requestDto,
                       HttpServletResponse response) {
        Long scheduleId = scheduleService.create(sessionMember.getId(), requestDto);

        response.addHeader(HttpHeaders.LOCATION, "/api/v1/schedules/" + scheduleId);
    }

    // 스케쥴 단건 조회
    @GetMapping("/{scheduleId}")
    public ScheduleResponseDto findById(@LoginMember SessionMember sessionMember,
                                        @PathVariable Long scheduleId) {
        return scheduleService.findById(sessionMember.getId(), scheduleId);
    }

    // 자신의 날짜별 스케쥴 리스트 조회
    @GetMapping("/day")
    public List<ScheduleListResponseDto> findAllByDate(
            @LoginMember SessionMember sessionMember,
            @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam LocalDate requestDate) {
        return scheduleService.findAllByDate(sessionMember.getId(), requestDate);
    }

    // 월별 스케쥴이 존재하는 날짜들의 목록 조회
    @GetMapping("/month")
    public List<ScheduleCountResponseDto> getCountOfDaysInMonth(
            @LoginMember SessionMember sessionMember,
            @DateTimeFormat(pattern = "yyyy-MM") @RequestParam YearMonth requestMonth) {
        return scheduleService.getCountOfDaysInMonth(sessionMember.getId(), requestMonth);
    }


    // 스케쥴 수정 (PUT - 전체 정보가 서버로 보내진다고 가정)
    @PutMapping("/{scheduleId}")
    public void edit(@LoginMember SessionMember sessionMember,
                     @PathVariable Long scheduleId,
                     @RequestBody @Valid ScheduleEditRequestDto requestDto) {
        scheduleService.edit(sessionMember.getId(), scheduleId, requestDto);
    }

    // 스케쥴 삭제
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{scheduleId}")
    public void delete(@LoginMember SessionMember sessionMember,
                       @PathVariable Long scheduleId) {
        scheduleService.delete(sessionMember.getId(), scheduleId);
    }
}
