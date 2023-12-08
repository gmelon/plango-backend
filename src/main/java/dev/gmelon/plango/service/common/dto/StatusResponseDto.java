package dev.gmelon.plango.service.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class StatusResponseDto {
    private String status;

    private int code;

    private StatusResponseDto(String status, int code) {
        this.status = status;
        this.code = code;
    }

    public static StatusResponseDto ok() {
        return new StatusResponseDto("OK", 200);
    }

    public static StatusResponseDto error() {
        return new StatusResponseDto("ERROR", 400);
    }
}
