package io.github.cctyl.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DataUtil {


    /**
     * 获取一个指定长度的随机数列表，支持指定边界，边界为[start,end]
     * @param size
     * @param start
     * @param end
     * @return
     */
    public static Set<Integer> getRandom(int size,int start,int end){
        if (start>=end || size<=0){
            throw new RuntimeException("参数异常");
        }
        Random random = new Random();
        HashSet<Integer> numSet = new HashSet<>(size);
        while (numSet.size()<size){
            int randomNumber = random.nextInt(end-start+1) + start;
            numSet.add(randomNumber);
        }

        return numSet;
    }

    /**
     * 获取一个指定范围的随机数
     * @param start
     * @param end
     * @return
     */
    public static int getRandom(int start,int end){
        Random random = new Random();
        return random.nextInt(end-start+1) + start;
    }
}
