package dev.gmelon.plango.domain.notification.repository;

import dev.gmelon.plango.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    void deleteAllInBatchByMemberId(Long memberId);

}
