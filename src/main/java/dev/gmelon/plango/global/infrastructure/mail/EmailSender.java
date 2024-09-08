package dev.gmelon.plango.global.infrastructure.mail;

import dev.gmelon.plango.global.infrastructure.mail.dto.EmailMessage;

public interface EmailSender {
    void send(EmailMessage emailMessage);
}
