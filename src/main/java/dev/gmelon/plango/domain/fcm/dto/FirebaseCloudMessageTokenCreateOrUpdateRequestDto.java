package dev.gmelon.plango.domain.fcm.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FirebaseCloudMessageTokenCreateOrUpdateRequestDto {

    @NotBlank
    private String tokenValue;

    @Builder
    public FirebaseCloudMessageTokenCreateOrUpdateRequestDto(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
