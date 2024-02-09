package dev.gmelon.plango.domain.schedule.exception;

import dev.gmelon.plango.global.exception.PlangoException;
import org.springframework.http.HttpStatus;

public class ScheduleOwnerParticipantDuplicateException extends PlangoException {

    private static final String MESSAGE = "일정을 생성한 회원이 일정의 참가자로 중복 등록되었습니다.";

    public ScheduleOwnerParticipantDuplicateException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
