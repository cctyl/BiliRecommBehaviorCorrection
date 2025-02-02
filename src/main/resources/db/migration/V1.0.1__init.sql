create table config
(
    id                 char(30)    not null
        constraint pk_config
            primary key,
    name               varchar(50) not null,
    value              varchar(200),
    expire_second      int        default -1,
    created_date       DATE,
    last_modified_date DATE,
    is_deleted         tinyint(1) default 0,
    version            int        default 1
);

create table cookie_header_data
(
    id         CHAR    not null
        constraint pk_cookie_header_data
            primary key,
    url        VARCHAR,
    ckey       VARCHAR not null,
    cvalue     VARCHAR not null,
    classify   VARCHAR,
    media_type VARCHAR
);

create table dict
(
    id                 char(30)     not null
        constraint pk_dict
            primary key,
    value              VARCHAR(255) not null,
    access_type        varchar(50),
    dict_type          varchar(50),
    outer_id           varchar(60),
    created_date       DATE,
    last_modified_date DATE,
    is_deleted         tinyint(1) default 0,
    version            int        default 1,
    desc               varchar(50)
);

create table owner
(
    id                 char(30)
        primary key,
    mid                VARCHAR(100) not null
        unique,
    name               VARCHAR(100) not null,
    face               VARCHAR(350),
    created_date       DATE,
    last_modified_date DATE
);

create table prepare_video
(
    id                 char(30)    not null
        constraint pk_prepare_video
            primary key,
    video_id           char(30)    not null,
    handle_type        varchar(30) not null,
    created_date       DATE,
    last_modified_date DATE,
    is_deleted         tinyint(1) default 0,
    version            int        default 1
);

create table stat
(
    id                 char(30) not null
        constraint pk_stat
            primary key,
    aid                INT      not null,
    view               INT,
    danmaku            INT,
    reply              INT,
    favorite           INT,
    coin               INT,
    share              INT,
    now_rank           INT,
    his_rank           INT,
    like               INT,
    dislike            INT,
    vt                 INT,
    vv                 INT,
    video_id           char(30),
    created_date       DATE,
    last_modified_date DATE
);

create table tag
(
    id                 char(30)     not null
        constraint pk_tag
            primary key,
    tag_id             int          not null,
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
    created_date       DATE,
    last_modified_date DATE,
    unique (tag_id, tag_name)
);

create table task
(
    id                 char(30) not null
        constraint pk_task
            primary key,
    last_run_time      DATE,
    current_run_status TEXT,
    total_run_count    int,
    last_run_duration  int,
    task_name          TEXT,
    scheduled_hour     int,
    is_enabled         int,
    class_method_name  text,
    description        text,
    img                text
);

create table video_detail
(
    id                   CHAR(30)     not null
        constraint pk_video_detail
            primary key,
    aid                  INT          not null
        unique,
    videos               INT,
    tid                  INT,
    tname                VARCHAR(255),
    copyright            INT,
    pic                  VARCHAR(350),
    title                VARCHAR(255),
    pubdate              INT,
    ctime                INT,
    desc                 VARCHAR(255),
    state                INT,
    duration             INT,
    mission_id           INT,
    dynamic              VARCHAR(255),
    cid                  INT,
    season_id            INT,
    short_link_v2        VARCHAR(255),
    first_frame          VARCHAR(255),
    pub_location         VARCHAR(255),
    bvid                 VARCHAR(255) not null
        unique,
    season_type          INT,
    is_ogv               TINYINT(1),
    owner_id             CHAR(30),
    handle               TINYINT(1) default 0,
    black_reason         text,
    thumb_up_reason      text,
    teenage_mode         INT,
    is_chargeable_season TINYINT(1),
    is_story             TINYINT(1),
    is_upower_exclusive  TINYINT(1),
    is_upower_play       TINYINT(1),
    no_cache             TINYINT(1),
    is_season_display    TINYINT(1),
    like_icon            VARCHAR(255),
    need_jump_bv         TINYINT(1),
    enable_vt            INT,
    disable_show_up_info TINYINT(1),
    up_from_v2           INT,
    rcmd_reason          VARCHAR(255),
    score                INT,
    handle_type          varchar(50),
    created_date         DATE,
    last_modified_date   DATE,
    is_deleted           tinyint(1) default 0,
    version              int        default 1
);

create index idx_aid
    on video_detail (aid);

create index idx_video_detail_handle_type
    on video_detail (handle_type);

create table video_relate
(
    id               char(30) not null
        constraint pk_video_tag
            primary key,
    master_video_id  char(30) not null,
    related_video_id char(30) not null
);

create table video_reply
(
    id                 char(30)      not null
        constraint pk_video_reply
            primary key,
    created_date       DATE,
    last_modified_date DATE,
    video_id           char(30)      not null,
    rpid               bigint        not null,
    oid                bigint        not null,
    mid                varchar(200)  not null,
    root               bigint        not null,
    parent             bigint        not null,
    dialog             bigint        not null,
    ctime              int           not null,
    current_level      int           not null,
    vip_type           int           not null,
    message            varchar(2000) not null,
    sex                varchar(20)
);

create table video_tag
(
    id                 char(30) not null
        constraint pk_video_tag
            primary key,
    tag_id             char(30) not null,
    video_id           char(30) not null,
    created_date       DATE,
    last_modified_date DATE,
    is_deleted         tinyint(1) default 0,
    version            int        default 1
);

create table white_list_rule
(
    id                 char(30) not null
        constraint pk_white_list_rule
            primary key,
    info               varchar(100),
    created_date       DATE,
    last_modified_date DATE,
    is_deleted         tinyint(1) default 0,
    version            int        default 1
);


insert into task (id, last_run_time, current_run_status, total_run_count, last_run_duration, task_name, scheduled_hour, is_enabled, class_method_name, description, img)
values  ('1', '1738465200025', 'STOPPED', 0, 0, '关键词搜索任务', 11, 1, 'io.github.cctyl.service.impl.BiliService.doSearchTask', '根据设定的关键词进行搜索，搜索的数据加入待处理列表', 'fas fa-search'),
        ('2', '1738328400042', 'STOPPED', 0, 0, '热门排行榜任务', 21, 1, 'io.github.cctyl.service.impl.BiliService.doHotRankTask', '抓取热门排行榜的数据，加入待处理列表', 'fas fa-chart-line'),
        ('3', '1738464340143', 'STOPPED', 0, 0, '首页推荐任务', 6, 1, 'io.github.cctyl.service.impl.BiliService.doHomeRecommendTask', '连续抓取首页排行榜的数据，加入待处理列表', 'fas fa-home'),
        ('4', '1738311862978', 'STOPPED', 0, 0, '处理视频-就按你说的做', 12, 0, 'io.github.cctyl.service.impl.BiliService.doDefaultProcessVideo', '按机器判断处理所有未人工审核的视频，程序初始判断的是白名单就按白名单处理，是黑名单就按黑名单处理', 'fas fa-robot'),
        ('5', '1738472400032', 'STOPPED', 0, 0, '处理视频-触发三次处理', 13, 1, 'io.github.cctyl.service.impl.BiliService.doThirdProcess', '进行三次处理，按照二次处理的结果对视频点赞或点踩，单次最大处理80条数据', 'fas fa-play-circle'),
        ('1883876188535939073', '1737986058762', 'STOPPED', 0, 0, null, -1, 0, 'io.github.cctyl.controller.ReplyController.saveVideoReplay', null, null),
        ('1883876237844176897', '1737986422145', 'STOPPED', 0, 0, null, -1, 0, 'io.github.cctyl.controller.BlackRuleController.dislikeByTid', null, null),
        ('1883876352046686210', '1737986097726', 'STOPPED', 0, 0, null, -1, 0, 'io.github.cctyl.controller.BlackRuleController.dislikeByUserId', null, null),
        ('1883876385634672642', '1737986105747', 'STOPPED', 0, 0, null, -1, 0, 'io.github.cctyl.controller.WhiteRuleController.thumbUpUserAllVideo', null, null),
        ('1883877101111631873', '1737986284913', 'STOPPED', 0, 0, null, -1, 0, 'io.github.cctyl.controller.WhiteRuleController.addTrain', null, null);


INSERT INTO cookie_header_data (id, url, ckey, cvalue, classify, media_type) VALUES ('658cf182905410d38abf75808ac01a2f', null, 'User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36', 'REQUEST_HEADER', 'GENERAL');
