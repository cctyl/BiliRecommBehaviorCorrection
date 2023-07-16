package io.github.cctyl.utils;

/**
 * id生成器
 */
public class IdGenerator {

    private static final SnowFlake snowFlake = new SnowFlake();

    /**
     * 获取一个id
     * @return
     */
    public static long nextId(){
        return snowFlake.nextId();
    }

}
