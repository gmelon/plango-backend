package dev.gmelon.plango.infrastructure.mail;

import dev.gmelon.plango.infrastructure.mail.dto.EmailMessage;

public interface EmailSender {
    void send(EmailMessage emailMessage);
}
