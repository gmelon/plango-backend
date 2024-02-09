package dev.gmelon.plango.domain.scheduleplace.controller;

import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceCreateRequestDto;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceEditRequestDto;
import dev.gmelon.plango.domain.scheduleplace.dto.SchedulePlaceSearchResponseDto;
import dev.gmelon.plango.domain.scheduleplace.service.SchedulePlaceService;
import dev.gmelon.plango.global.config.auth.LoginMember;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
