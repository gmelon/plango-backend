package dev.gmelon.plango.web.fcm;

import dev.gmelon.plango.config.auth.LoginMember;
import dev.gmelon.plango.service.fcm.FirebaseCloudMessageTokenService;
import dev.gmelon.plango.service.fcm.dto.FirebaseCloudMessageTokenCreateOrUpdateRequestDto;
import dev.gmelon.plango.service.fcm.dto.FirebaseCloudMessageTokenDeleteRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/fcm/tokens")
@RestController
public class FirebaseCloudMessageTokenController  {

    private final FirebaseCloudMessageTokenService firebaseCloudMessageTokenService;

    @PostMapping
    public void createOrUpdate(@LoginMember Long memberId,
                       @RequestBody @Valid FirebaseCloudMessageTokenCreateOrUpdateRequestDto requestDto) {
        firebaseCloudMessageTokenService.createOrUpdate(memberId, requestDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@LoginMember Long memberId,
                       @RequestBody @Valid FirebaseCloudMessageTokenDeleteRequestDto requestDto) {
        firebaseCloudMessageTokenService.delete(memberId, requestDto);
    }

}
