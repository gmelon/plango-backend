package dev.gmelon.plango.exception.mail;

import dev.gmelon.plango.exception.InternalServerException;

public class MailSendFailureException extends InternalServerException {
    private static final String MESSAGE = "메일 발송에 실패했습니다.";

    public MailSendFailureException() {
        super(MESSAGE);
    }
}
