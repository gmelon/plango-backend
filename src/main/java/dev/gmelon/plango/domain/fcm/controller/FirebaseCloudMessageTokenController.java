package dev.gmelon.plango.domain.fcm.controller;

import dev.gmelon.plango.domain.fcm.dto.FirebaseCloudMessageTokenCreateOrUpdateRequestDto;
import dev.gmelon.plango.domain.fcm.dto.FirebaseCloudMessageTokenDeleteRequestDto;
import dev.gmelon.plango.domain.fcm.service.FirebaseCloudMessageTokenService;
import dev.gmelon.plango.global.config.auth.LoginMember;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
