package dev.gmelon.plango.web.schedule.place;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.schedule.place.SchedulePlaceService;
import dev.gmelon.plango.service.schedule.place.dto.SchedulePlaceCreateRequestDto;
import dev.gmelon.plango.service.schedule.place.dto.SchedulePlaceEditRequestDto;
import dev.gmelon.plango.service.schedule.place.dto.SchedulePlaceSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class SchedulePlaceController {

    private final SchedulePlaceService schedulePlaceService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/schedules/{scheduleId}/places")
    public void add(@LoginMember Long memberId,
                    @PathVariable Long scheduleId, @Valid @RequestBody SchedulePlaceCreateRequestDto requestDto) {
        schedulePlaceService.add(memberId, scheduleId, requestDto);
    }

    @PutMapping("/api/schedules/{scheduleId}/places/{placeId}")
    public void edit(@LoginMember Long memberId, @PathVariable Long scheduleId,
                     @PathVariable Long placeId, @Valid @RequestBody SchedulePlaceEditRequestDto requestDto) {
        schedulePlaceService.edit(memberId, scheduleId, placeId, requestDto);

    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/schedules/{scheduleId}/places/{placeId}")
    public void remove(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.remove(memberId, scheduleId, placeId);
    }

    @PostMapping("/api/schedules/{scheduleId}/places/{placeId}/confirm")
    public void confirm(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.confirm(memberId, scheduleId, placeId);
    }

    @DeleteMapping("/api/schedules/{scheduleId}/places/{placeId}/confirm")
    public void deny(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.deny(memberId, scheduleId, placeId);
    }

    @PostMapping("/api/schedules/{scheduleId}/places/{placeId}/like")
    public void like(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.like(memberId, scheduleId, placeId);
    }

    @DeleteMapping("/api/schedules/{scheduleId}/places/{placeId}/like")
    public void dislike(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.dislike(memberId, scheduleId, placeId);
    }

    @GetMapping(value = "/api/schedulePlaces", params = "query")
    public List<SchedulePlaceSearchResponseDto> search(
            @LoginMember Long memberId,
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page) {
        return schedulePlaceService.search(memberId, query, page);
    }

}
