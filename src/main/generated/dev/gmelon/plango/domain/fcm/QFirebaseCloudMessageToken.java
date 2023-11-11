package dev.gmelon.plango.domain.fcm;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFirebaseCloudMessageToken is a Querydsl query type for FirebaseCloudMessageToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFirebaseCloudMessageToken extends EntityPathBase<FirebaseCloudMessageToken> {

    private static final long serialVersionUID = -1181503220L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFirebaseCloudMessageToken firebaseCloudMessageToken = new QFirebaseCloudMessageToken("firebaseCloudMessageToken");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastUpdatedDate = createDateTime("lastUpdatedDate", java.time.LocalDateTime.class);

    public final dev.gmelon.plango.domain.member.QMember member;

    public final StringPath tokenValue = createString("tokenValue");

    public QFirebaseCloudMessageToken(String variable) {
        this(FirebaseCloudMessageToken.class, forVariable(variable), INITS);
    }

    public QFirebaseCloudMessageToken(Path<? extends FirebaseCloudMessageToken> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFirebaseCloudMessageToken(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFirebaseCloudMessageToken(PathMetadata metadata, PathInits inits) {
        this(FirebaseCloudMessageToken.class, metadata, inits);
    }

    public QFirebaseCloudMessageToken(Class<? extends FirebaseCloudMessageToken> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new dev.gmelon.plango.domain.member.QMember(forProperty("member")) : null;
    }

}

