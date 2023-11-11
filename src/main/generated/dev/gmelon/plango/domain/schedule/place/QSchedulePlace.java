package dev.gmelon.plango.domain.schedule.place;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSchedulePlace is a Querydsl query type for SchedulePlace
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSchedulePlace extends EntityPathBase<SchedulePlace> {

    private static final long serialVersionUID = -1146447800L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSchedulePlace schedulePlace = new QSchedulePlace("schedulePlace");

    public final dev.gmelon.plango.domain.QBaseTimeEntity _super = new dev.gmelon.plango.domain.QBaseTimeEntity(this);

    public final StringPath category = createString("category");

    public final BooleanPath confirmed = createBoolean("confirmed");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath memo = createString("memo");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final StringPath placeName = createString("placeName");

    public final StringPath roadAddress = createString("roadAddress");

    public final dev.gmelon.plango.domain.schedule.QSchedule schedule;

    public final SetPath<SchedulePlaceLike, QSchedulePlaceLike> schedulePlaceLikes = this.<SchedulePlaceLike, QSchedulePlaceLike>createSet("schedulePlaceLikes", SchedulePlaceLike.class, QSchedulePlaceLike.class, PathInits.DIRECT2);

    public QSchedulePlace(String variable) {
        this(SchedulePlace.class, forVariable(variable), INITS);
    }

    public QSchedulePlace(Path<? extends SchedulePlace> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSchedulePlace(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSchedulePlace(PathMetadata metadata, PathInits inits) {
        this(SchedulePlace.class, metadata, inits);
    }

    public QSchedulePlace(Class<? extends SchedulePlace> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.schedule = inits.isInitialized("schedule") ? new dev.gmelon.plango.domain.schedule.QSchedule(forProperty("schedule")) : null;
    }

}

