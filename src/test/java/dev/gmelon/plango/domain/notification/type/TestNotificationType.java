package dev.gmelon.plango.domain.notification.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestNotificationType implements NotificationType {

    SCHEDULE_INVITED("일정 제목 - %s", "%s님이 %s님을 일정에 초대했습니다.", 1, 2);

    private final String titleMessageFormat;
    private final String contentMessageFormat;
    private final int titleArgsCount;
    private final int contentArgsCount;

}
