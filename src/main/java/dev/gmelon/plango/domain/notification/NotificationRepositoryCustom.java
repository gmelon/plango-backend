package dev.gmelon.plango.domain.notification;

import java.util.List;

public interface NotificationRepositoryCustom {

    List<Notification> findAllByMemberId(Long memberId, int page);

}
