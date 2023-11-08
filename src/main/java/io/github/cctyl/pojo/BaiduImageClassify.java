package io.github.cctyl.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
{
  "person_num": 2,
  "person_info": [
    {
      "attributes": {
        "orientation": {
          "score": "0.5762208104133606",
          "name": "右侧面"
        },
        "gender": {
          "score": "0.8896589875221252",
          "name": "女性"
        },
        "umbrella": {
          "score": "0.9996577501296997",
          "name": "未打伞"
        },
        "lower_color": {
          "score": "0.9105194807052612",
          "name": "蓝"
        },
        "face_mask": {
          "score": "0.9871914386749268",
          "name": "无口罩"
        },
        "smoke": {
          "score": "0.9948787689208984",
          "name": "未吸烟"
        },
        "bag": {
          "score": "0.6357678174972534",
          "name": "无背包"
        },
        "upper_wear": {
          "score": "0.9648274183273315",
          "name": "短袖"
        },
        "is_human": {
          "score": "0.9525250196456909",
          "name": "正常人体"
        },
        "vehicle": {
          "score": "0.9988948702812195",
          "name": "无交通工具"
        },
        "glasses": {
          "score": "0.8623613715171814",
          "name": "无眼镜"
        },
        "headwear": {
          "score": "0.8920268416404724",
          "name": "无帽"
        },
        "upper_wear_fg": {
          "score": "0.8092058300971985",
          "name": "无袖"
        },
        "upper_wear_texture": {
          "score": "0.7602768540382385",
          "name": "纯色"
        },
        "upper_cut": {
          "score": "0.9889894127845764",
          "name": "无上方截断"
        },
        "occlusion": {
          "score": "0.9727711081504822",
          "name": "无遮挡"
        },
        "lower_cut": {
          "score": "0.8755531311035156",
          "name": "有下方截断"
        },
        "cellphone": {
          "score": "0.9999989867210388",
          "name": "看手机"
        },
        "carrying_item": {
          "score": "0.7435759305953979",
          "name": "无手提物"
        },
        "age": {
          "score": "0.8416159749031067",
          "name": "青年"
        },
        "lower_wear": {
          "score": "0.4350847601890564",
          "name": "长裤"
        },
        "upper_color": {
          "score": "0.640583336353302",
          "name": "黑"
        }
      },
      "location": {
        "score": "0.8655313849449158",
        "top": 187,
        "left": 733,
        "width": 509,
        "height": 1373
      }
    },
    {
      "attributes": {
        "orientation": {
          "score": "0.987069845199585",
          "name": "正面"
        },
        "gender": {
          "score": "0.946491539478302",
          "name": "女性"
        },
        "umbrella": {
          "score": "0.9998362064361572",
          "name": "未打伞"
        },
        "lower_color": {
          "score": "0.7132999300956726",
          "name": "蓝"
        },
        "face_mask": {
          "score": "0.9990146160125732",
          "name": "无口罩"
        },
        "smoke": {
          "score": "0.9474381804466248",
          "name": "未吸烟"
        },
        "bag": {
          "score": "0.7949902415275574",
          "name": "无背包"
        },
        "upper_wear": {
          "score": "0.9274871945381165",
          "name": "短袖"
        },
        "is_human": {
          "score": "0.9847004413604736",
          "name": "正常人体"
        },
        "vehicle": {
          "score": "0.999461829662323",
          "name": "无交通工具"
        },
        "glasses": {
          "score": "0.9541480541229248",
          "name": "无眼镜"
        },
        "headwear": {
          "score": "0.8193402886390686",
          "name": "无帽"
        },
        "upper_wear_fg": {
          "score": "0.9749122262001038",
          "name": "无袖"
        },
        "upper_wear_texture": {
          "score": "0.4992052316665649",
          "name": "纯色"
        },
        "upper_cut": {
          "score": "0.9996223449707031",
          "name": "无上方截断"
        },
        "occlusion": {
          "score": "0.8746844530105591",
          "name": "无遮挡"
        },
        "lower_cut": {
          "score": "0.7095136046409607",
          "name": "有下方截断"
        },
        "cellphone": {
          "score": "0.7970471978187561",
          "name": "未使用手机"
        },
        "carrying_item": {
          "score": "0.5824993252754211",
          "name": "不确定"
        },
        "age": {
          "score": "0.8534162640571594",
          "name": "青年"
        },
        "lower_wear": {
          "score": "0.4942257404327393",
          "name": "不确定"
        },
        "upper_color": {
          "score": "0.8473121523857117",
          "name": "黑"
        }
      },
      "location": {
        "score": "0.7146634459495544",
        "top": 192,
        "left": 1294,
        "width": 454,
        "height": 1361
      }
    }
  ]
}

 */
@NoArgsConstructor
@Data
public class BaiduImageClassify {
    /**
     * 人数
     */
    @JSONField(name = "person_num")
    private Integer personNum;

    /**
     * 识别到的人体信息
     */
    @JSONField(name = "person_info")
    private List<PersonInfo> personInfo;


    @JSONField(name = "log_id")
    private String logId;

    @NoArgsConstructor
    @Data
    public static class PersonInfo {
        @JSONField(name = "attributes")
        private Attributes attributes;
        @JSONField(name = "location")
        private Location location;
    }
    @NoArgsConstructor
    @Data
    public static class Attributes {
        @JSONField(name = "orientation")
        private Orientation orientation;
        @JSONField(name = "gender")
        private Gender gender;
        @JSONField(name = "umbrella")
        private Umbrella umbrella;
        @JSONField(name = "lower_color")
        private LowerColor lowerColor;
        @JSONField(name = "face_mask")
        private FaceMask faceMask;
        @JSONField(name = "smoke")
        private Smoke smoke;
        @JSONField(name = "bag")
        private Bag bag;
        @JSONField(name = "upper_wear")
        private UpperWear upperWear;
        @JSONField(name = "is_human")
        private IsHuman isHuman;
        @JSONField(name = "vehicle")
        private Vehicle vehicle;
        @JSONField(name = "glasses")
        private Glasses glasses;
        @JSONField(name = "headwear")
        private Headwear headwear;
        @JSONField(name = "upper_wear_fg")
        private UpperWearFg upperWearFg;
        @JSONField(name = "upper_wear_texture")
        private UpperWearTexture upperWearTexture;
        @JSONField(name = "upper_cut")
        private UpperCut upperCut;
        @JSONField(name = "occlusion")
        private Occlusion occlusion;
        @JSONField(name = "lower_cut")
        private LowerCut lowerCut;
        @JSONField(name = "cellphone")
        private Cellphone cellphone;
        @JSONField(name = "carrying_item")
        private CarryingItem carryingItem;
        @JSONField(name = "age")
        private Age age;
        @JSONField(name = "lower_wear")
        private LowerWear lowerWear;
        @JSONField(name = "upper_color")
        private UpperColor upperColor;

        @NoArgsConstructor
        @Data
        public static class Orientation {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Gender {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Umbrella {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class LowerColor {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class FaceMask {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Smoke {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Bag {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class UpperWear {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class IsHuman {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Vehicle {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Glasses {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Headwear {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class UpperWearFg {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class UpperWearTexture {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class UpperCut {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Occlusion {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class LowerCut {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Cellphone {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class CarryingItem {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class Age {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class LowerWear {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }

        @NoArgsConstructor
        @Data
        public static class UpperColor {
            @JSONField(name = "score")
            private String score;
            @JSONField(name = "name")
            private String name;
        }
    }

    @NoArgsConstructor
    @Data
    public static class Location {
        @JSONField(name = "score")
        private String score;
        @JSONField(name = "top")
        private Integer top;
        @JSONField(name = "left")
        private Integer left;
        @JSONField(name = "width")
        private Integer width;
        @JSONField(name = "height")
        private Integer height;
    }
}
