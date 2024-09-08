package dev.gmelon.plango.domain.notification.exception;

import dev.gmelon.plango.global.exception.InternalServerException;

public class IllegalNotificationArgumentCountException extends InternalServerException {

    private static final String MESSAGE_FORMAT = "알림 발송을 위해 %d개의 인자가 필요하지만, %d개의 인자만 전달되었습니다.";

    public IllegalNotificationArgumentCountException(int expectedArgsCount, int actualArgsCount) {
        super(String.format(MESSAGE_FORMAT, expectedArgsCount, actualArgsCount));
    }

}
