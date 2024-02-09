package dev.gmelon.plango.domain.notification.repository;

import dev.gmelon.plango.domain.notification.entity.Notification;
import java.util.List;

public interface NotificationRepositoryCustom {

    List<Notification> findAllByMemberId(Long memberId, int page);

}
