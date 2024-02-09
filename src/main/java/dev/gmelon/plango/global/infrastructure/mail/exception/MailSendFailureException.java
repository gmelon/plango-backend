package dev.gmelon.plango.global.infrastructure.mail.exception;

import dev.gmelon.plango.global.exception.InternalServerException;

public class MailSendFailureException extends InternalServerException {
    private static final String MESSAGE = "메일 발송에 실패했습니다.";

    public MailSendFailureException() {
        super(MESSAGE);
    }
}
