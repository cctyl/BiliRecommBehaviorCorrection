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
    public static void sleep2Second(){
        sleep(2);
    }
    public static void sleep5Second(){
        sleep(5);
    }
    public static void sleep1Second(){
        sleep(1);
    }
    public static void sleep10Second(){
        sleep(10);
    }
    public static void sleep20Second(){
        sleep(20);
    }
}
