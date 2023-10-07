package dev.gmelon.plango.web.schedule.place;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.schedule.place.SchedulePlaceService;
import dev.gmelon.plango.service.schedule.place.dto.SchedulePlaceCreateRequestDto;
import dev.gmelon.plango.service.schedule.place.dto.SchedulePlaceEditRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/schedules/{scheduleId}/places")
@RestController
public class SchedulePlaceController {

    private final SchedulePlaceService schedulePlaceService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void add(@LoginMember Long memberId,
                    @PathVariable Long scheduleId, @Valid @RequestBody SchedulePlaceCreateRequestDto requestDto) {
        schedulePlaceService.add(memberId, scheduleId, requestDto);
    }

    @PutMapping("/{placeId}")
    public void edit(@LoginMember Long memberId, @PathVariable Long scheduleId,
                     @PathVariable Long placeId, @Valid @RequestBody SchedulePlaceEditRequestDto requestDto) {
        schedulePlaceService.edit(memberId, scheduleId, placeId, requestDto);

    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{placeId}")
    public void remove(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.remove(memberId, scheduleId, placeId);
    }

    @PostMapping("/{placeId}/confirm")
    public void confirm(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.confirm(memberId, scheduleId, placeId);
    }

    @DeleteMapping("/{placeId}/confirm")
    public void deny(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.deny(memberId, scheduleId, placeId);
    }

    @PostMapping("/{placeId}/like")
    public void like(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.like(memberId, scheduleId, placeId);
    }

    @DeleteMapping("/{placeId}/like")
    public void dislike(@LoginMember Long memberId, @PathVariable Long scheduleId, @PathVariable Long placeId) {
        schedulePlaceService.dislike(memberId, scheduleId, placeId);
    }

}
