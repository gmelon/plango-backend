package dev.gmelon.plango.exception.notification;

import dev.gmelon.plango.exception.InternalServerException;

public class IllegalNotificationArgumentCountException extends InternalServerException {

    private static final String MESSAGE_FORMAT = "알림 발송을 위해 %d개의 인자가 필요하지만, %d개의 인자만 전달되었습니다.";

    public IllegalNotificationArgumentCountException(int expectedArgsCount, int actualArgsCount) {
        super(String.format(MESSAGE_FORMAT, expectedArgsCount, actualArgsCount));
    }

}
