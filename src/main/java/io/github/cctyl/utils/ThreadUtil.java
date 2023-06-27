package io.github.cctyl.utils;

public class ThreadUtil {


    /**
     * 线程休眠
     * @param second
     */
    public static void sleep(int second){
        try {
            Thread.sleep(second*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
