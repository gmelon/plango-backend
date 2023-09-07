package dev.gmelon.plango.domain.notification;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static dev.gmelon.plango.domain.notification.QNotification.notification;
import static java.lang.Math.max;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private static final int DEFAULT_PAGINATION_SIZE = 40;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Notification> findAllByMemberId(Long memberId, int page) {
        return jpaQueryFactory.selectFrom(notification)
                .where(notification.member.id.eq(memberId))
                .limit(DEFAULT_PAGINATION_SIZE)
                .offset(offset(page))
                .orderBy(notification.createdTime.desc())
                .fetch();
    }

    private int offset(int page) {
        return (max(1, page) - 1) * DEFAULT_PAGINATION_SIZE;
    }
}
