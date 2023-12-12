drop table if exists diary_image CASCADE;
drop table if exists diary CASCADE;
drop table if exists member CASCADE;
drop table if exists schedule CASCADE;
drop table if exists place_search_record CASCADE;

create table diary_image
(
    id            bigint not null auto_increment,
    created_time  timestamp,
    modified_date timestamp,
    image_url     varchar(255),
    diary_id      bigint,
    primary key (id),
    foreign key (diary_id) references diary (id)
);
create table diary
(
    id            bigint not null auto_increment,
    created_time  timestamp,
    modified_date timestamp,
    content       TEXT,
    member_id     bigint,
    schedule_id   bigint,
    primary key (id),
    foreign key (member_id) references member (id),
    foreign key (schedule_id) references schedule (id)
);
create table member
(
    id                bigint            not null auto_increment,
    created_time      timestamp,
    modified_date     timestamp,
    email             varchar(255)      not null,
    nickname          varchar(255)      not null,
    bio               varchar(50),
    profile_image_url varchar(255),
    password          varchar(255)      not null,
    terms_accepted    BOOLEAN DEFAULT 0 not null,
    type              varchar(255)      not null,
    primary key (id),
    unique (email),
    unique (nickname)
);
create table schedule
(
    id            bigint            not null auto_increment,
    created_time  timestamp,
    modified_date timestamp,
    date          date,
    start_time    time,
    end_time      time,
    content       TEXT,
    done          BOOLEAN DEFAULT 0 not null,
    latitude      double,
    longitude     double,
    road_address  varchar(255),
    place_name    varchar(255),
    title         varchar(255)      not null,
    primary key (id)
);
create table schedule_member
(
    id          bigint            not null auto_increment,
    accepted    BOOLEAN DEFAULT 0 not null,
    owner       BOOLEAN DEFAULT 0 not null,
    member_id   bigint,
    schedule_id bigint,
    primary key (id),
    foreign key (member_id) references member (id),
    foreign key (schedule_id) references schedule (id)
);
create table place_search_record
(
    id                 bigint                              not null auto_increment,
    keyword            varchar(255)                        not null,
    center_latitude    double,
    center_longitude   double,
    last_searched_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null,
    member_id          bigint,
    primary key (id),
    foreign key (member_id) references member (id)
);
create table notification
(
    id                bigint not null auto_increment,
    created_time      timestamp,
    modified_date     timestamp,
    argument          varchar(255),
    content           varchar(255),
    notification_type varchar(255),
    title             varchar(255),
    member_id         bigint,
    primary key (id),
    foreign key (member_id) references member (id)
);
create table firebase_cloud_message_token
(
    id                bigint       not null auto_increment,
    last_updated_date timestamp    not null,
    token_value       varchar(255) not null,
    member_id         bigint,
    primary key (id),
    foreign key (member_id) references member (id)
);
create table schedule_place
(
    id            bigint            not null auto_increment,
    created_time  timestamp,
    modified_date timestamp,
    category      varchar(10),
    confirmed     BOOLEAN DEFAULT 0 not null,
    latitude      double,
    longitude     double,
    memo          varchar(255),
    place_name    varchar(255)      not null,
    road_address  varchar(255),
    schedule_id   bigint,
    primary key (id),
    foreign key (schedule_id) references schedule (id)
);
create table schedule_place_like
(
    id                bigint not null auto_increment,
    created_time      timestamp,
    modified_date     timestamp,
    member_id         bigint,
    schedule_place_id bigint,
    primary key (id),
    foreign key (member_id) references member (id),
    foreign key (schedule_place_id) references schedule_place (id)
);

-- security
create table persistent_logins
(
    username  varchar(64) not null,
    series    varchar(64) primary key,
    token     varchar(64) not null,
    last_used timestamp   not null
);
