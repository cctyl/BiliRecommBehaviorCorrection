CREATE TABLE video_reply
(
    id char(30) NOT NULL,
    created_date DATE null,
    last_modified_date DATE null,
--    视频id
    video_id char(30) not null ,
--	评论id
    rpid bigint not null,
--    评论区对象 id
    oid bigint not null,
--	发送者 mid
    mid varchar(200) not null,
--	根评论 rpid
--	若为一级评论则为 0
--	大于一级评论则为根评论 id
    root bigint not null,
--	回复父评论 rpid
--	若为一级评论则为 0
--	若为二级评论则为根评论 rpid
--	大于二级评论为上一级评 论 rpid
    parent bigint not null,
--	回复对方 rpid
--	若为一级评论则为 0
--	若为二级评论则为该评论 rpid
--	大于二级评论为上一级评论 rpid
    dialog bigint not null,
--	评论发送时间
    ctime int not null,
-- 评论者等级
    current_level int not null,
-- 评论者vip状态
    vip_type int not null,
--    评论信息
    message varchar(2000) not null,


-- 性别
    sex varchar(20),

    CONSTRAINT pk_video_reply PRIMARY KEY (id)
);
