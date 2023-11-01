package dev.gmelon.plango.service.place;

import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.member.MemberRepository;
import dev.gmelon.plango.domain.place.PlaceSearchRecord;
import dev.gmelon.plango.domain.place.PlaceSearchRecordRepository;
import dev.gmelon.plango.exception.member.NoSuchMemberException;
import dev.gmelon.plango.exception.place.NoSuchPlaceSearchRecordException;
import dev.gmelon.plango.exception.place.PlaceSearchRecordAccessDeniedException;
import dev.gmelon.plango.service.place.dto.PlaceSearchRecordListResponseDto;
import dev.gmelon.plango.service.place.dto.PlaceSearchRecordRequestDto;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PlaceSearchRecordService {

    private final PlaceSearchRecordRepository placeSearchRecordRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public List<PlaceSearchRecordListResponseDto> findAll(Long memberId, int page) {
        return placeSearchRecordRepository.findAllByMemberId(memberId, page).stream()
                .map(PlaceSearchRecordListResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void search(Long memberId, PlaceSearchRecordRequestDto requestDto) {
        Member member = findMemberById(memberId);

        PlaceSearchRecord placeSearchRecord = findPlaceSearchRecordOrCreate(requestDto, member);
        placeSearchRecord.search(LocalDateTime.now(clock));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NoSuchMemberException::new);
    }

    private PlaceSearchRecord findPlaceSearchRecordOrCreate(PlaceSearchRecordRequestDto requestDto, Member member) {
        return placeSearchRecordRepository.findByKeywordAndMemberId(requestDto.getKeyword(), member.getId())
                .orElseGet(() -> placeSearchRecordRepository.save(requestDto.toEntity(member, LocalDateTime.now(clock))));
    }

    @Transactional
    public void delete(Long memberId, Long recordId) {
        PlaceSearchRecord placeSearchRecord = findPlaceSearchRecordById(recordId);

        validateMember(placeSearchRecord, memberId);

        placeSearchRecordRepository.deleteById(recordId);
    }

    private PlaceSearchRecord findPlaceSearchRecordById(Long recordId) {
        return placeSearchRecordRepository.findById(recordId)
                .orElseThrow(NoSuchPlaceSearchRecordException::new);
    }

    private void validateMember(PlaceSearchRecord placeSearchRecord, Long memberId) {
        if (!placeSearchRecord.memberId().equals(memberId)) {
            throw new PlaceSearchRecordAccessDeniedException();
        }
    }
}
