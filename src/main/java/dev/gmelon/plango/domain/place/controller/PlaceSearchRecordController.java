package dev.gmelon.plango.domain.place.controller;

import dev.gmelon.plango.domain.place.dto.PlaceSearchRecordListResponseDto;
import dev.gmelon.plango.domain.place.dto.PlaceSearchRecordRequestDto;
import dev.gmelon.plango.domain.place.service.PlaceSearchRecordService;
import dev.gmelon.plango.global.config.auth.LoginMember;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/place/search-records")
@RestController
public class PlaceSearchRecordController {

    private final PlaceSearchRecordService placeSearchRecordService;

    @GetMapping
    public List<PlaceSearchRecordListResponseDto> findAll(@LoginMember Long memberId, @RequestParam(defaultValue = "1") int page) {
        return placeSearchRecordService.findAll(memberId, page);
    }

    @PostMapping
    public void search(@LoginMember Long memberId,
                       @RequestBody @Valid PlaceSearchRecordRequestDto requestDto) {
        placeSearchRecordService.search(memberId, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{recordId}")
    public void delete(@LoginMember Long memberId, @PathVariable Long recordId) {
        placeSearchRecordService.delete(memberId, recordId);
    }

}
