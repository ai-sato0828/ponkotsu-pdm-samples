create table order_registration (
    request_id varchar(64) primary key,
    status varchar(32) not null
);

create table review_record (
    request_id varchar(64) primary key,
    result varchar(32) not null
);
