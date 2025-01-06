package io.github.cctyl.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
https://github.com/SocialSisterYi/bilibili-API-collect/blob/fd313b8c1cd2dcbcaf22c5aa68930dbf9354b690/grpc_api/bilibili/app/card/v1/common.proto#L56
message Base {
    // 卡片类型
    string card_type = 1;
    // 卡片跳转类型?
    string card_goto = 2;
    // 跳转类型
    // av:视频稿件 mid:用户空间
    string goto = 3;
    // 目标参数
    string param = 4;
    // 封面url
    string cover = 5;
    // 标题
    string title = 6;
    // 跳转uri
    string uri = 7;
    //
    ThreePoint three_point = 8;
    //
    Args args = 9;
    //
    PlayerArgs player_args = 10;
    // 条目排位序号
    int64 idx = 11;
    //
    AdInfo ad_info = 12;
    //
    Mask mask = 13;
    //来源标识
    // recommend:推荐 operation:管理?
    string from_type = 14;
    //
    repeated ThreePointV2 three_point_v2 = 15;
    //
    repeated ThreePointV3 three_point_v3 = 16;
    //
    Button desc_button = 17;
    // 三点v4
    ThreePointV4 three_point_v4 = 18;
    //
    UpArgs up_args = 19;
}
 */
@NoArgsConstructor
@Data
public class RecommendCard {
    @JSONField(name = "card_type")
    private String cardType;
    @JSONField(name = "card_goto")
    private String cardGoto;
    @JSONField(name = "param")
    private String param;
    @JSONField(name = "cover")
    private String cover;
    @JSONField(name = "title")
    private String title;
    @JSONField(name = "uri")
    private String uri;
    @JSONField(name = "three_point")
    private ThreePoint threePoint;
    @JSONField(name = "args")
    private Args args;
    @JSONField(name = "player_args")
    private PlayerArgs playerArgs;
    @JSONField(name = "idx")
    private Long idx;
    @JSONField(name = "three_point_v2")
    private List<ThreePointV2> threePointV2;
    @JSONField(name = "track_id")
    private String trackId;
    @JSONField(name = "talk_back")
    private String talkBack;
    @JSONField(name = "cover_left_text_1")
    private String coverLeftText1;
    @JSONField(name = "cover_left_icon_1")
    private Integer coverLeftIcon1;
    @JSONField(name = "cover_left_1_content_description")
    private String coverLeft1ContentDescription;
    @JSONField(name = "cover_left_text_2")
    private String coverLeftText2;
    @JSONField(name = "cover_left_icon_2")
    private Integer coverLeftIcon2;
    @JSONField(name = "cover_left_2_content_description")
    private String coverLeft2ContentDescription;
    @JSONField(name = "cover_right_text")
    private String coverRightText;
    @JSONField(name = "cover_right_content_description")
    private String coverRightContentDescription;
    @JSONField(name = "desc_button")
    private DescButton descButton;
    @JSONField(name = "can_play")
    private Integer canPlay;
    @JSONField(name = "goto_icon")
    private GotoIcon gotoIcon;

    @NoArgsConstructor
    @Data
    public static class ThreePoint {
        @JSONField(name = "dislike_reasons")
        private List<DislikeReasons> dislikeReasons;
        @JSONField(name = "feedbacks")
        private List<Feedbacks> feedbacks;
        @JSONField(name = "watch_later")
        private Integer watchLater;

        @NoArgsConstructor
        @Data
        public static class DislikeReasons {
            @JSONField(name = "id")
            private Integer id;
            @JSONField(name = "name")
            private String name;
            @JSONField(name = "toast")
            private String toast;
        }

        @NoArgsConstructor
        @Data
        public static class Feedbacks {
            @JSONField(name = "id")
            private Integer id;
            @JSONField(name = "name")
            private String name;
            @JSONField(name = "toast")
            private String toast;
        }
    }

    @NoArgsConstructor
    @Data
    public static class Args {
        @JSONField(name = "up_id")
        private Long upId;
        @JSONField(name = "up_name")
        private String upName;
        @JSONField(name = "rid")
        private Long rid;
        @JSONField(name = "rname")
        private String rname;
        @JSONField(name = "tid")
        private Long tid;
        @JSONField(name = "tname")
        private String tname;
        @JSONField(name = "aid")
        private Long aid;
    }

    @NoArgsConstructor
    @Data
    public static class PlayerArgs {
        @JSONField(name = "aid")
        private Long aid;
        @JSONField(name = "cid")
        private Long cid;
        @JSONField(name = "type")
        private String type;
        @JSONField(name = "duration")
        private Integer duration;
    }

    @NoArgsConstructor
    @Data
    public static class DescButton {
        @JSONField(name = "text")
        private String text;
        @JSONField(name = "uri")
        private String uri;
        @JSONField(name = "event")
        private String event;
        @JSONField(name = "type")
        private Integer type;
    }

    @NoArgsConstructor
    @Data
    public static class GotoIcon {
        @JSONField(name = "icon_url")
        private String iconUrl;
        @JSONField(name = "icon_night_url")
        private String iconNightUrl;
        @JSONField(name = "icon_width")
        private Integer iconWidth;
        @JSONField(name = "icon_height")
        private Integer iconHeight;
    }

    @NoArgsConstructor
    @Data
    public static class ThreePointV2 {
        @JSONField(name = "title")
        private String title;
        @JSONField(name = "type")
        private String type;
        @JSONField(name = "icon")
        private String icon;
        @JSONField(name = "subtitle")
        private String subtitle;
        @JSONField(name = "reasons")
        private List<Reasons> reasons;

        @NoArgsConstructor
        @Data
        public static class Reasons {
            @JSONField(name = "id")
            private Long id;
            @JSONField(name = "name")
            private String name;
            @JSONField(name = "toast")
            private String toast;
        }
    }
}
