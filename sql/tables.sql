drop table if exists diary CASCADE;
drop table if exists member CASCADE;
drop table if exists schedule CASCADE;

create table diary (id bigint not null auto_increment, created_time timestamp, modified_date timestamp, content TEXT, image_url varchar(255), title varchar(255), primary key (id));
create table member (id bigint not null auto_increment, created_time timestamp, modified_date timestamp, email varchar(255) not null, nickname varchar(255) not null, password varchar(255) not null, primary key (id),
                        unique (email), unique (nickname));
create table schedule (id bigint not null auto_increment, created_time timestamp, modified_date timestamp, content TEXT, done BOOLEAN DEFAULT 0 not null, end_time timestamp not null, latitude double, longitude double, road_address varchar(255), place_name varchar(255), start_time timestamp not null, title varchar(255) not null, diary_id bigint, member_id bigint,
                        primary key (id), foreign key (diary_id) references diary(id), foreign key (member_id) references member(id));

# security
create table persistent_logins (username varchar(64) not null, series varchar(64) primary key, token varchar(64) not null, last_used timestamp not null);
