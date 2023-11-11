package dev.gmelon.plango.domain.schedule.place;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSchedulePlaceLike is a Querydsl query type for SchedulePlaceLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSchedulePlaceLike extends EntityPathBase<SchedulePlaceLike> {

    private static final long serialVersionUID = 951670783L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSchedulePlaceLike schedulePlaceLike = new QSchedulePlaceLike("schedulePlaceLike");

    public final dev.gmelon.plango.domain.QBaseTimeEntity _super = new dev.gmelon.plango.domain.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final dev.gmelon.plango.domain.member.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final QSchedulePlace schedulePlace;

    public QSchedulePlaceLike(String variable) {
        this(SchedulePlaceLike.class, forVariable(variable), INITS);
    }

    public QSchedulePlaceLike(Path<? extends SchedulePlaceLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSchedulePlaceLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSchedulePlaceLike(PathMetadata metadata, PathInits inits) {
        this(SchedulePlaceLike.class, metadata, inits);
    }

    public QSchedulePlaceLike(Class<? extends SchedulePlaceLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new dev.gmelon.plango.domain.member.QMember(forProperty("member")) : null;
        this.schedulePlace = inits.isInitialized("schedulePlace") ? new QSchedulePlace(forProperty("schedulePlace"), inits.get("schedulePlace")) : null;
    }

}

