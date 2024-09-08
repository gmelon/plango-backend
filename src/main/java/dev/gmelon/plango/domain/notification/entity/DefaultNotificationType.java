package dev.gmelon.plango.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DefaultNotificationType implements NotificationType {

    SCHEDULE_INVITED("%s", "일정에 초대되었습니다. 앱에서 일정 내용을 확인해보세요.", 1, 0),
    SCHEDULE_ACCEPTED("%s", "%s님이 일정 초대를 수락했습니다.", 1, 1),
    SCHEDULE_EXITED_BY_PARTICIPANT("%s", "%s님이 일정을 나갔습니다.", 1, 1),
    SCHEDULE_EXITED_BY_OWNER("%s", "일정에서 탈퇴되었습니다.", 1, 0),
    SCHEDULE_EDITED("%s", "%s님이 일정을 수정했습니다.", 1, 1),
    SCHEDULE_DELETED("%s", "일정이 삭제되었습니다.", 1, 0);

    private final String titleMessageFormat;
    private final String contentMessageFormat;
    private final int titleArgsCount;
    private final int contentArgsCount;

}
