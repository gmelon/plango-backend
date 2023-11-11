package dev.gmelon.plango.config.auth.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPersistentLogins is a Querydsl query type for PersistentLogins
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPersistentLogins extends EntityPathBase<PersistentLogins> {

    private static final long serialVersionUID = 1129866717L;

    public static final QPersistentLogins persistentLogins = new QPersistentLogins("persistentLogins");

    public final DateTimePath<java.time.LocalDateTime> lastUsed = createDateTime("lastUsed", java.time.LocalDateTime.class);

    public final StringPath series = createString("series");

    public final StringPath token = createString("token");

    public final StringPath username = createString("username");

    public QPersistentLogins(String variable) {
        super(PersistentLogins.class, forVariable(variable));
    }

    public QPersistentLogins(Path<? extends PersistentLogins> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPersistentLogins(PathMetadata metadata) {
        super(PersistentLogins.class, metadata);
    }

}

