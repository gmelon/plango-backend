package dev.gmelon.plango.service.fcm.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class FirebaseCloudMessageTokenDeleteRequestDto {

    @NotBlank
    private String tokenValue;

    @Builder
    public FirebaseCloudMessageTokenDeleteRequestDto(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
