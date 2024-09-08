package dev.gmelon.plango.global.infrastructure.mail;

import dev.gmelon.plango.global.infrastructure.mail.dto.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile({"local", "test"})
@Component
public class ConsoleEmailSender implements EmailSender {
    @Override
    public void send(EmailMessage emailMessage) {
        log.info("email 전송 완료. content : \n{}", emailMessage.getContent());
    }
}
