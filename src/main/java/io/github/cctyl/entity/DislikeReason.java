package io.github.cctyl.entity;

import lombok.Data;

/**
 *  {
 *    "id": 4,
 *    "name": "UP主:白云猫狗",
 *    "toast": "将减少相似内容推荐"
 *  },
 *  {
 *    "id": 2,
 *    "name": "分区:喵星人",
 *    "toast": "将减少相似内容推荐"
 *  },
 *  {
 *    "id": 3,
 *    "name": "频道:猫咪",
 *    "toast": "将减少相似内容推荐"
 *  },
 *  {
 *    "id": 12,
 *    "name": "此类内容过多",
 *    "toast": "将减少相似内容推荐"
 *  },
 *  {
 *    "id": 13,
 *    "name": "推荐过",
 *    "toast": "将减少相似内容推荐"
 *  },
 *  {
 *    "id": 1,
 *    "name": "不感兴趣",
 *    "toast": "将减少相似内容推荐"
 *  }
 */
@Data
public class DislikeReason {
    private int id;
    private String name;
    private String toast;


    public DislikeReason(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 对up主不感兴趣
     * @param userName
     * @return
     */
    public static DislikeReason up(String userName){
        return new DislikeReason(4,"UP主:"+userName);
    }


    /**
     * 对分区不感兴趣
     * @return
     */
    public static DislikeReason tid(String tName){
        return new DislikeReason(2,"分区："+tName);
    }

    /**
     * 对频道不感兴趣，相当于tag
     * @return
     */
    public static DislikeReason channel(){
        return new DislikeReason(3,"频道不感兴趣");
    }


    /**
     * 相似内容过多
     * @return
     */
    public static DislikeReason tooMuch(){
        return new DislikeReason(12,"相似内容过多");
    }


    /**
     * 已推荐过
     * @return
     */
    public static DislikeReason commended(){
        return new DislikeReason(13,"推荐过");
    }

    /**
     * 不感兴趣
     * @return
     */
    public static DislikeReason notInteresting(){
        return new DislikeReason(1,"不感兴趣");
    }


}
