package io.github.cctyl.domain.enumeration;


public enum DictType {


    /**
     * 标签类型
     */
    TAG,

    /**
     * 描述
     */
    DESC,

    /**
     * 标题
     */
    TITLE,

    /**
     * 封面
     */
    COVER,

    /**
     * up主id
     */
    MID,

    /**
     * 分区id
     */
    TID,

    /**
     * 搜索词
     */
    SEARCH_KEYWORD,

    /**
     * 通用的关键词，对于没有细分是标题还是封面还是描述的关键词等使用的类型
     */
    KEYWORD,

    /**
     * 需要忽略的关键词
     */
    IGNORE_KEYWORD,

    /**
     * 停顿词
     */
    STOP_WORDS,


}
