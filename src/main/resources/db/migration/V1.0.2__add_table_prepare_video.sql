CREATE TABLE prepare_video
(
    id                 char(30)    NOT NULL,
    video_id           char(30)    not null,
    handle_type        varchar(30) not null,
    created_date       DATE        null,
    last_modified_date DATE        null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    CONSTRAINT pk_prepare_video PRIMARY KEY (id)
);

