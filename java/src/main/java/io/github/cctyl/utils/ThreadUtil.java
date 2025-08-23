package io.github.cctyl.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadUtil {


    /**
     * 线程休眠
     * @param second
     */
    public static void sleep(int second){
        try {
            Thread.sleep(second* 1000L);
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
        }
    }
    public static void s2(){
        sleep(2);
    }
    public static void s5(){
        sleep(5);
    }
    public static void s1(){
        sleep(1);
    }
    public static void s10(){
        sleep(10);
    }
    public static void s20(){
        sleep(20);
    }
    public static void s30(){
        sleep(30);
    }
}
