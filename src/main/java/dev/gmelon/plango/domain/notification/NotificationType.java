package dev.gmelon.plango.domain.notification;

import lombok.Getter;

@Getter
public enum NotificationType {

    SCHEDULE_INVITED("%s", "일정에 초대되었습니다. 앱에서 일정 내용을 확인해보세요."),
    SCHEDULE_REJECTED_BY_PARTICIPANT("%s", "%s님이 일정 수락을 거절했습니다."),
    SCHEDULE_EXITED_BY_PARTICIPANT("%s", "%s님이 일정을 나갔습니다."),
    SCHEDULE_EXITED_BY_OWNER("%s", "일정에서 탈퇴되었습니다."),
    SCHEDULE_EDITED("%s", "일정이 수정되었습니다.");

    private final String titleMessageFormat;
    private final String contentMessageFormat;

    NotificationType(String titleMessageFormat, String contentMessageFormat) {
        this.titleMessageFormat = titleMessageFormat;
        this.contentMessageFormat = contentMessageFormat;
    }

    public String formatTitle(Object... args) {
        return String.format(titleMessageFormat, args);
    }

    public String formatContent(Object... args) {
        return String.format(contentMessageFormat, args);
    }

}
