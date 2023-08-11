package dev.gmelon.plango.web.place;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.place.PlaceSearchRecordService;
import dev.gmelon.plango.service.place.dto.PlaceSearchRecordListResponseDto;
import dev.gmelon.plango.service.place.dto.PlaceSearchRecordRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
