package io.github.cctyl.utils;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class DataUtil {


    /**
     * av号转换bv号
     * 参考 https://github.com/SocialSisterYi/bilibili-API-collect/blob/b52187a48bd05bf985b70ae7fe3afde38c254052/docs/misc/bvid_desc.md
     * @param aid
     * @return bvId
     */
    public static String aidToBvid(Long aid) {
        return AVBVConverter.av2bv(aid);
    }

    /**
     * bv转换av号
     * 参考 https://github.com/SocialSisterYi/bilibili-API-collect/blob/b52187a48bd05bf985b70ae7fe3afde38c254052/docs/misc/bvid_desc.md
     * @param bvid
     * @return avid
     */
    public static Long bvidToAid(String bvid) {
       return AVBVConverter.bv2av(bvid);
    }

    /**
     * 获取一个指定长度的随机数列表，支持指定边界，边界为[start,end]
     *
     * @param size
     * @param start
     * @param end
     * @return
     */
    public static Set<Integer> getRandom(int size, int start, int end) {
        if (start >= end || size <= 0) {
            throw new RuntimeException("参数异常");
        }
        Random random = new Random();
        HashSet<Integer> numSet = new HashSet<>(size);
        while (numSet.size() < size) {
            int randomNumber = random.nextInt(end - start + 1) + start;
            numSet.add(randomNumber);
        }

        return numSet;
    }

    /**
     * 随机访问List中的元素
     *
     * @param source   源数据
     * @param size     需要抽取多少个元素
     * @param consumer
     * @param <T>
     */
    public static <T> void randomAccessList(final List<T> source,
                                            int size,
                                            Consumer<T> consumer
    ) {
        if (size >= source.size()) {
            size = source.size() - 1;
        }
        DataUtil
                .getRandom(size, 0, size)
                .stream().map(source::get)
                .forEach(consumer);
    }

    /**
     * 获取一个指定范围的随机数
     *
     * @param start
     * @param end
     * @return
     */
    public static int getRandom(int start, int end) {
        Random random = new Random();
        return random.nextInt(end - start + 1) + start;
    }

    /**
     * 获取url上的query 参数
     *
     * @param url
     * @param paramName
     * @return
     */
    public static String getUrlQueryParam(String url, String paramName) {

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
     *
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
            log.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 将cookie字符串转换为key-value形式
     * @param cookieStr
     * @return
     */
    public static Map<String, String> splitCookie(String cookieStr) {
        Map<String, String> map = new HashMap<>(10);
        String[] cookieArr = cookieStr.split(";");
        for (String cookie : cookieArr) {
            String[] split = cookie.split("=");
            map.put(split[0].trim(), split[1].trim());
        }
        return map;
    }


    /**
     * 获得url中的host
     * @param url
     * @return
     */
    public static String getHost(String url){
        String noProtocol = url.replaceAll("https://", "")
                .replace("http://", "");
        int i = noProtocol
                .indexOf("/");
        if (i==-1){
            return noProtocol;
        }else {
           return   noProtocol.substring(0,i);
        }

    }


    /**
     * 统计该key的出现频率
     * @param frequencyMap
     * @param key
     */
    public static<K> void countFrequency(Map<K,Integer> frequencyMap,K key){
        Integer count = frequencyMap.getOrDefault(key,0);
        frequencyMap.put(key,++count);
    }


    public static Integer calculateSecondsDifference(Date date1, Date date2) {
        long time1 = date1.getTime();
        long time2 = date2.getTime();
        long diff = Math.abs(time2 - time1);
        return Math.toIntExact(diff / 1000);
    }


    /**
     * 循环获取数据
     * @param startPage 起始页
     * @param pageSize 每页大小，用于判断是否查询完毕了，若返回记录数量小于每页大小，说明数据快要结束了
     * @param getDataFunction 获取数据的方法，pageNo传入页码，pageSize传入每页大小,返回记录
     * @param afterPageGet  每一页获取完毕后要执行的操作
     * @param maxLoopNum  最大循环次数，null 表示直到查询结束
     * @return 所有获取到的记录
     * @param <T> 数据类型
     */
    public static<T> List<T> eachGetPageData(
            int startPage,
            int pageSize,
            Integer maxLoopNum,
            BiFunction<Integer,Integer,List<T>> getDataFunction,
            Consumer<List<T>> afterPageGet
       ){


        int loopNum = 0;
        List<T> result = new LinkedList<>();
        while (true){
            loopNum++;
            List<T> list = getDataFunction.apply(startPage++,pageSize);

            if (list!=null){
               result.addAll(list);
                log.info("第{}次读取得到{}条数据",loopNum,list.size());
            }else {
                log.info("第{}次读取得到0条数据",loopNum);
            }
            afterPageGet.accept(list);
            if (
                    //已经没有数据了
                    CollUtil.isEmpty(list)
                    //记录已经小于每页大小
                    || list.size() < pageSize
                    //有循环次数，且已经达到循环次数，则退出
                    || (maxLoopNum !=null && loopNum >= maxLoopNum)
            ) {
                break;
            }
        }
        return result;
    }
}
