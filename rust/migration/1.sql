ALTER TABLE "config" RENAME TO "_config_old_20250827";

CREATE TABLE "config" (
  "id" char(30) NOT NULL,
  "name" varchar(50) NOT NULL,
  "value" varchar(200),
  "expire_second" int DEFAULT -1,
  "created_date" DATE,
  "last_modified_date" DATE,
  CONSTRAINT "pk_config" PRIMARY KEY ("id")
);

INSERT INTO "config" ("id", "name", "value", "expire_second", "created_date", "last_modified_date") SELECT "id", "name", "value", "expire_second", "created_date", "last_modified_date" FROM "_config_old_20250827";



--- -------------------------


ALTER TABLE "dict" RENAME TO "_dict_old_20250827";

CREATE TABLE "dict" (
  "id" char(30) NOT NULL,
  "value" VARCHAR(255) NOT NULL,
  "access_type" varchar(50),
  "dict_type" varchar(50),
  "outer_id" varchar(60),
  "created_date" DATE,
  "last_modified_date" DATE,
  "desc" varchar(50),
  CONSTRAINT "pk_dict" PRIMARY KEY ("id")
);

INSERT INTO "dict" ("id", "value", "access_type", "dict_type", "outer_id", "created_date", "last_modified_date", "desc") SELECT "id", "value", "access_type", "dict_type", "outer_id", "created_date", "last_modified_date", "desc" FROM "_dict_old_20250827";

---------------------------------------------

ALTER TABLE "prepare_video" RENAME TO "_prepare_video_old_20250827";

CREATE TABLE "prepare_video" (
  "id" char(30) NOT NULL,
  "video_id" char(30) NOT NULL,
  "handle_type" varchar(30) NOT NULL,
  "created_date" DATE,
  "last_modified_date" DATE,
  CONSTRAINT "pk_prepare_video" PRIMARY KEY ("id")
);

INSERT INTO "prepare_video" ("id", "video_id", "handle_type", "created_date", "last_modified_date") SELECT "id", "video_id", "handle_type", "created_date", "last_modified_date" FROM "_prepare_video_old_20250827";




ALTER TABLE "tag" RENAME TO "_tag_old_20250827";

CREATE TABLE "tag" (
  "id" char(30) NOT NULL,
  "tag_id" int NOT NULL,
  "tag_name" varchar(255) NOT NULL,
  "content" VARCHAR(150),
  CONSTRAINT "pk_tag" PRIMARY KEY ("id"),
  UNIQUE ("tag_id" ASC, "tag_name" ASC)
);

INSERT INTO "tag" ("id", "tag_id", "tag_name", "content") SELECT "id", "tag_id", "tag_name", "content" FROM "_tag_old_20250827";


ALTER TABLE "owner" RENAME TO "_owner_old_20250828";

CREATE TABLE "owner" (
  "id" char(30),
  "mid" VARCHAR(100) NOT NULL,
  "name" VARCHAR(100) NOT NULL,
  "face" VARCHAR(350),
  PRIMARY KEY ("id"),
  UNIQUE ("mid" ASC)
);

INSERT INTO "owner" ("id", "mid", "name", "face") SELECT "id", "mid", "name", "face" FROM "_owner_old_20250828";


ALTER TABLE "stat" RENAME TO "_stat_old_20250828";

CREATE TABLE "stat" (
  "id" char(30) NOT NULL,
  "aid" INT NOT NULL,
  "view" INT,
  "danmaku" INT,
  "reply" INT,
  "favorite" INT,
  "coin" INT,
  "share" INT,
  "now_rank" INT,
  "his_rank" INT,
  "like" INT,
  "dislike" INT,
  "video_id" char(30),
  "created_date" DATE,
  "last_modified_date" DATE,
  CONSTRAINT "pk_stat" PRIMARY KEY ("id")
);

INSERT INTO "stat" ("id", "aid", "view", "danmaku", "reply", "favorite", "coin", "share", "now_rank", "his_rank", "like", "dislike", "video_id", "created_date", "last_modified_date") SELECT "id", "aid", "view", "danmaku", "reply", "favorite", "coin", "share", "now_rank", "his_rank", "like", "dislike", "video_id", "created_date", "last_modified_date" FROM "_stat_old_20250828";




DROP INDEX "idx_aid";

DROP INDEX "idx_video_detail_handle_type";

ALTER TABLE "video_detail" RENAME TO "_video_detail_old_20250828";

CREATE TABLE "video_detail" (
  "id" CHAR(30) NOT NULL,
  "aid" INT NOT NULL,
  "videos" INT,
  "tid" INT,
  "tname" VARCHAR(255),
  "copyright" INT,
  "pic" VARCHAR(350),
  "title" VARCHAR(255),
  "pubdate" INT,
  "ctime" INT,
  "desc" VARCHAR(255),
  "state" INT,
  "duration" INT,
  "dynamic" VARCHAR(255),
  "cid" INT,
  "season_id" INT,
  "first_frame" VARCHAR(255),
  "pub_location" VARCHAR(255),
  "bvid" VARCHAR(255) NOT NULL,
  "owner_id" CHAR(30),
  "handle" TINYINT(1) DEFAULT 0,
  "black_reason" text,
  "thumb_up_reason" text,
  "teenage_mode" INT,
  "no_cache" TINYINT(1),
  "up_from_v2" INT,
  "rcmd_reason" VARCHAR(255),
  "handle_type" varchar(50),
  "created_date" DATE,
  "last_modified_date" DATE,
  "is_deleted" tinyint(1) DEFAULT 0,
  CONSTRAINT "pk_video_detail" PRIMARY KEY ("id"),
  UNIQUE ("aid" ASC),
  UNIQUE ("bvid" ASC)
);

INSERT INTO "video_detail" ("id", "aid", "videos", "tid", "tname", "copyright", "pic", "title", "pubdate", "ctime", "desc", "state", "duration", "dynamic", "cid", "season_id", "first_frame", "pub_location", "bvid", "owner_id", "handle", "black_reason", "thumb_up_reason", "teenage_mode", "no_cache", "up_from_v2", "rcmd_reason", "handle_type", "created_date", "last_modified_date", "is_deleted") SELECT "id", "aid", "videos", "tid", "tname", "copyright", "pic", "title", "pubdate", "ctime", "desc", "state", "duration", "dynamic", "cid", "season_id", "first_frame", "pub_location", "bvid", "owner_id", "handle", "black_reason", "thumb_up_reason", "teenage_mode", "no_cache", "up_from_v2", "rcmd_reason", "handle_type", "created_date", "last_modified_date", "is_deleted" FROM "_video_detail_old_20250828";

CREATE INDEX "idx_aid"
ON "video_detail" (
  "aid" ASC
);

CREATE INDEX "idx_video_detail_handle_type"
ON "video_detail" (
  "handle_type" ASC
);


DROP INDEX "idx_aid";

DROP INDEX "idx_video_detail_handle_type";

ALTER TABLE "video_detail" RENAME TO "_video_detail_old_20250828_1";

CREATE TABLE "video_detail" (
  "id" CHAR(30) NOT NULL,
  "aid" INT NOT NULL,
  "videos" INT,
  "tid" INT,
  "tname" VARCHAR(255),
  "copyright" INT,
  "pic" VARCHAR(350),
  "title" VARCHAR(255),
  "pubdate" INT,
  "ctime" INT,
  "desc" VARCHAR(255),
  "state" INT,
  "duration" INT,
  "dynamic" VARCHAR(255),
  "cid" INT,
  "season_id" INT,
  "first_frame" VARCHAR(255),
  "pub_location" VARCHAR(255),
  "bvid" VARCHAR(255) NOT NULL,
  "owner_id" CHAR(30),
  "handle" TINYINT(1) DEFAULT 0,
  "black_reason" text,
  "thumb_up_reason" text,
  "no_cache" TINYINT(1),
  "up_from_v2" INT,
  "rcmd_reason" VARCHAR(255),
  "handle_type" varchar(50),
  "created_date" DATE,
  "last_modified_date" DATE,
  "is_deleted" tinyint(1) DEFAULT 0,
  CONSTRAINT "pk_video_detail" PRIMARY KEY ("id"),
  UNIQUE ("aid" ASC),
  UNIQUE ("bvid" ASC)
);

INSERT INTO "video_detail" ("id", "aid", "videos", "tid", "tname", "copyright", "pic", "title", "pubdate", "ctime", "desc", "state", "duration", "dynamic", "cid", "season_id", "first_frame", "pub_location", "bvid", "owner_id", "handle", "black_reason", "thumb_up_reason", "no_cache", "up_from_v2", "rcmd_reason", "handle_type", "created_date", "last_modified_date", "is_deleted") SELECT "id", "aid", "videos", "tid", "tname", "copyright", "pic", "title", "pubdate", "ctime", "desc", "state", "duration", "dynamic", "cid", "season_id", "first_frame", "pub_location", "bvid", "owner_id", "handle", "black_reason", "thumb_up_reason", "no_cache", "up_from_v2", "rcmd_reason", "handle_type", "created_date", "last_modified_date", "is_deleted" FROM "_video_detail_old_20250828_1";

CREATE INDEX "idx_aid"
ON "video_detail" (
  "aid" ASC
);

CREATE INDEX "idx_video_detail_handle_type"
ON "video_detail" (
  "handle_type" ASC
);


ALTER TABLE "video_tag" RENAME TO "_video_tag_old_20250828";

CREATE TABLE "video_tag" (
  "id" char(30) NOT NULL,
  "tag_id" char(30) NOT NULL,
  "video_id" char(30) NOT NULL,
  "created_date" DATE,
  CONSTRAINT "pk_video_tag" PRIMARY KEY ("id")
);

INSERT INTO "video_tag" ("id", "tag_id", "video_id", "created_date") SELECT "id", "tag_id", "video_id", "created_date" FROM "_video_tag_old_20250828";


drop table _config_old_20250827;
drop table _dict_old_20250827;
drop table _owner_old_20250828;
drop table _prepare_video_old_20250827;
drop table _stat_old_20250828;
drop table _tag_old_20250827;
drop table _video_detail_old_20250828;
drop table _video_detail_old_20250828_1;
drop table _video_tag_old_20250828;




CREATE TABLE if not exists "migration" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "version" integer NOT NULL,
  "created_time" DATE NOT NULL
);
