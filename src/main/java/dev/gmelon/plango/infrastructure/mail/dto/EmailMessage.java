package dev.gmelon.plango.infrastructure.mail.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EmailMessage {
    private final String to;

    private final String subject;

    private final String content;
}
