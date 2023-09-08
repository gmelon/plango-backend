package dev.gmelon.plango.exception.schedule;

import dev.gmelon.plango.exception.InternalServerException;

public class ScheduleOwnerNotExistsException extends InternalServerException {

    private static final String MESSAGE = "일정에 소유자가 존재하지 않습니다.";

    public ScheduleOwnerNotExistsException() {
        super(MESSAGE);
    }
}
