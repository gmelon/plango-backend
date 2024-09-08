package dev.gmelon.plango.global.infrastructure.mail;

import dev.gmelon.plango.global.infrastructure.mail.dto.EmailMessage;
import dev.gmelon.plango.global.infrastructure.mail.exception.MailSendFailureException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("real")
@RequiredArgsConstructor
@Component
public class HtmlEmailSender implements EmailSender {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderMail;

    @Override
    public void send(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false,
                    StandardCharsets.UTF_8.name());
            mimeMessageHelper.setFrom(senderMail, "Plango");
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getContent(), true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException | UnsupportedEncodingException exception) {
            log.error("email 발송에 실패했습니다.", exception);
            throw new MailSendFailureException();
        }
    }
}
