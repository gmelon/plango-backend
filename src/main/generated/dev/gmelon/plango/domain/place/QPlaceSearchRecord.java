package dev.gmelon.plango.domain.place;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlaceSearchRecord is a Querydsl query type for PlaceSearchRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlaceSearchRecord extends EntityPathBase<PlaceSearchRecord> {

    private static final long serialVersionUID = -1557271165L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPlaceSearchRecord placeSearchRecord = new QPlaceSearchRecord("placeSearchRecord");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public final DateTimePath<java.time.LocalDateTime> lastSearchedDate = createDateTime("lastSearchedDate", java.time.LocalDateTime.class);

    public final dev.gmelon.plango.domain.member.QMember member;

    public QPlaceSearchRecord(String variable) {
        this(PlaceSearchRecord.class, forVariable(variable), INITS);
    }

    public QPlaceSearchRecord(Path<? extends PlaceSearchRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPlaceSearchRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPlaceSearchRecord(PathMetadata metadata, PathInits inits) {
        this(PlaceSearchRecord.class, metadata, inits);
    }

    public QPlaceSearchRecord(Class<? extends PlaceSearchRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new dev.gmelon.plango.domain.member.QMember(forProperty("member")) : null;
    }

}

