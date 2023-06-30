package io.github.cctyl.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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

    /**
     * 获取url上的query 参数
     * @param url
     * @param paramName
     * @return
     */
    public static String getUrlQueryParam(String url,String paramName){

        try {
            URI uri = new URI(url);
            String query = uri.getQuery(); // 获取URL中的查询部分
            String[] queryParams = query.split("&"); // 将查询部分按 & 符号拆分成键值对
            return Arrays.stream(queryParams).filter(s -> s.contains(paramName))
                    .map(s -> s.split("=")[1])
                    .findFirst().orElseThrow(() -> new RuntimeException(" url 解析错误，未找到指定参数"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 返回一个16进制字符串表示的md5值
     * @param input
     * @return
     */
    public static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
