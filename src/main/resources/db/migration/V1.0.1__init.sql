-- dict definition

CREATE TABLE dict
(
    id                 char(30)     NOT NULL,
    value              VARCHAR(255) NULL,
    access_type        varchar(50)  null,
    dict_type          varchar(50)  null,
    outer_id           varchar(60)  NULL,

    created_date       DATE         null,
    last_modified_date DATE         null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    CONSTRAINT pk_dict PRIMARY KEY (id)
);


-- owner definition

CREATE TABLE owner
(
    id                 char(30) PRIMARY KEY,
    mid                VARCHAR(100) unique not null,
    name               VARCHAR(100)        not null,
    face               VARCHAR(350)        NULL,
    created_date       DATE                null,
    last_modified_date DATE                null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1

);


-- stat definition

CREATE TABLE stat
(
    id                 char(30) NOT NULL,
    aid                INT      NULL,
    view               INT      NULL,
    danmaku            INT      NULL,
    reply              INT      NULL,
    favorite           INT      NULL,
    coin               INT      NULL,
    share              INT      NULL,
    now_rank           INT      NULL,
    his_rank           INT      NULL,
    `like`             INT      NULL,
    dislike            INT      NULL,
    vt                 INT      NULL,
    vv                 INT      NULL,
    video_id           char(30) NULL,
    created_date       DATE     null,
    last_modified_date DATE     null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    CONSTRAINT pk_stat PRIMARY KEY (id)
);


-- video_detail definition

CREATE TABLE video_detail
(
    id                   CHAR(30)     NOT NULL,
    aid                  INT          not null unique,
    videos               INT          NULL,
    tid                  INT          NULL,
    tname                VARCHAR(255) NULL,
    copyright            INT          NULL,
    pic                  VARCHAR(350) NULL,
    title                VARCHAR(255) NULL,
    pubdate              INT          NULL,
    ctime                INT          NULL,
    `desc`               VARCHAR(255) NULL,
    state                INT          NULL,
    duration             INT          NULL,
    mission_id           INT          NULL,
    dynamic              VARCHAR(255) NULL,
    cid                  INT          NULL,
    season_id            INT          NULL,
    short_link_v2        VARCHAR(255) NULL,
    first_frame          VARCHAR(255) NULL,
    pub_location         VARCHAR(255) NULL,
    bvid                 VARCHAR(255) not null unique,
    season_type          INT          NULL,
    is_ogv               TINYINT(1)   NULL,
    owner_id             CHAR(30)     NULL,
    handle               TINYINT(1) default 0,
    black_reason         text         null,
    thumb_up_reason      text         null,
    teenage_mode         INT          NULL,
    is_chargeable_season TINYINT(1)   NULL,
    is_story             TINYINT(1)   NULL,
    is_upower_exclusive  TINYINT(1)   NULL,
    is_upower_play       TINYINT(1)   NULL,
    no_cache             TINYINT(1)   NULL,
    is_season_display    TINYINT(1)   NULL,
    like_icon            VARCHAR(255) NULL,
    need_jump_bv         TINYINT(1)   NULL,
    enable_vt            INT          NULL,
    disable_show_up_info TINYINT(1)   NULL,
    up_from_v2           INT          NULL,
    rcmd_reason          VARCHAR(255) NULL,
    score                INT          NULL,
    handle_type          varchar(50)  null,
    created_date         DATE         null,
    last_modified_date   DATE         null,
    is_deleted           tinyint(1) default 0,
    version              int        default 1,

    CONSTRAINT pk_video_detail PRIMARY KEY (id)
);


-- white_list_rule definition

CREATE TABLE white_list_rule
(
    id                 char(30)     NOT NULL,
    info               varchar(100) null,
    created_date       DATE         null,
    last_modified_date DATE         null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    CONSTRAINT pk_white_list_rule PRIMARY KEY (id)
);

-- tag
CREATE TABLE tag
(
    id                 char(30)     NOT NULL,
    tag_id             int unique   not null,
    tag_name           varchar(255) not null,
    cover              VARCHAR(350),
    head_cover         VARCHAR(350),
    content            VARCHAR(150),
    short_content      VARCHAR(100),
    type               INTEGER,
    state              INTEGER,
    ctime              INTEGER,
    is_atten           INTEGER,
    likes              INTEGER,
    hates              INTEGER,
    attribute          INTEGER,
    liked              INTEGER,
    hated              INTEGER,
    extra_attr         INTEGER,
    music_id           VARCHAR(255),
    tag_type           VARCHAR(255),
    is_activity        BOOLEAN,
    color              VARCHAR(255),
    alpha              INTEGER,
    is_season          BOOLEAN,
    subscribed_count   INTEGER,
    archive_count      VARCHAR(50),
    featured_count     INTEGER,
    jump_url           VARCHAR(350),
    created_date       DATE         null,
    last_modified_date DATE         null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    CONSTRAINT pk_tag PRIMARY KEY (id)
);

-- video与tag 关联表
CREATE TABLE video_tag
(

    id                 char(30) NOT NULL,
    tag_id             char(30) not null,
    video_id           char(30) not null,
    created_date       DATE     null,
    last_modified_date DATE     null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    CONSTRAINT pk_video_tag PRIMARY KEY (id)
);

CREATE TABLE cookie_header_data
(
    id                 char(30)     NOT NULL,
    url                VARCHAR(350) NULL,
    ckey               varchar(50)  null,
    cvalue             varchar(200) null,
    classify           varchar(50)  null,
    media_type          varchar(50)  null,
    created_date       DATE         null,
    last_modified_date DATE         null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    CONSTRAINT pk_cookie_header_data PRIMARY KEY (id)
);


CREATE TABLE config
(
    id                 char(30)     NOT NULL,
    name               varchar(50)  null,
    value              varchar(200) null,
    expire_second      int        default -1,
    created_date       DATE         null,
    last_modified_date DATE         null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    CONSTRAINT pk_config PRIMARY KEY (id)
);

-- 视频 与 相关视频 的关联表
create table video_relate
(

    id                 char(30) not null,
    master_video_id    char(30) not null,
    related_video_id   char(30) not null,
    created_date       DATE     null,
    last_modified_date DATE     null,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    constraint pk_video_tag primary key (id)
);