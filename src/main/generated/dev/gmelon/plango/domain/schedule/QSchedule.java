package dev.gmelon.plango.domain.schedule;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSchedule is a Querydsl query type for Schedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSchedule extends EntityPathBase<Schedule> {

    private static final long serialVersionUID = 1975888824L;

    public static final QSchedule schedule = new QSchedule("schedule");

    public final dev.gmelon.plango.domain.QBaseTimeEntity _super = new dev.gmelon.plango.domain.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final ListPath<dev.gmelon.plango.domain.diary.Diary, dev.gmelon.plango.domain.diary.QDiary> diaries = this.<dev.gmelon.plango.domain.diary.Diary, dev.gmelon.plango.domain.diary.QDiary>createList("diaries", dev.gmelon.plango.domain.diary.Diary.class, dev.gmelon.plango.domain.diary.QDiary.class, PathInits.DIRECT2);

    public final BooleanPath done = createBoolean("done");

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final ListPath<ScheduleMember, QScheduleMember> scheduleMembers = this.<ScheduleMember, QScheduleMember>createList("scheduleMembers", ScheduleMember.class, QScheduleMember.class, PathInits.DIRECT2);

    public final ListPath<dev.gmelon.plango.domain.schedule.place.SchedulePlace, dev.gmelon.plango.domain.schedule.place.QSchedulePlace> schedulePlaces = this.<dev.gmelon.plango.domain.schedule.place.SchedulePlace, dev.gmelon.plango.domain.schedule.place.QSchedulePlace>createList("schedulePlaces", dev.gmelon.plango.domain.schedule.place.SchedulePlace.class, dev.gmelon.plango.domain.schedule.place.QSchedulePlace.class, PathInits.DIRECT2);

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public final StringPath title = createString("title");

    public QSchedule(String variable) {
        super(Schedule.class, forVariable(variable));
    }

    public QSchedule(Path<? extends Schedule> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSchedule(PathMetadata metadata) {
        super(Schedule.class, metadata);
    }

}

