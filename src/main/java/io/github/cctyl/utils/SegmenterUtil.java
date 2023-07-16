package io.github.cctyl.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;
import io.github.cctyl.config.BeanProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.cctyl.constants.AppConstant.STOP_WORDS_KEY;

/**
 * 结巴分词工具
 */
public class SegmenterUtil {

    private static final JiebaSegmenter jiebaSegmenter  = new JiebaSegmenter();


    private static RedisUtil redisUtil;

    /**
     * 匹配标点符号的正则
     */
    private static final Pattern punctuationPattern = Pattern.compile("[=,.?!@#$%^&*()_+:\"<>/\\[\\]\\\\`~——，。、～《》？；’：“【】、{}|·！￥…（）-]");

    /**
     * 分词
     * @param str
     * @return
     */
    public static List<String> process(String str){
        return  jiebaSegmenter.process(str, JiebaSegmenter.SegMode.SEARCH)
                .stream().map(segToken -> segToken.word).collect(Collectors.toList());
    }

    /**
     * 获取前5个出现频率最高的词
     * @param keywordFrequencyMap
     * @return
     */
    public static List<String> getTop5FrequentWord(Map<String, Integer> keywordFrequencyMap) {
        return keywordFrequencyMap.entrySet().stream()
                .sorted((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 生成词频统计map
     *
     * @param titleProcess
     * @return
     */
    public static Map<String, Integer> generateFrequencyMap(List<String> titleProcess) {

        if (redisUtil==null){
            redisUtil = BeanProvider.getApplicationContext().getBean(RedisUtil.class);
        }

        HashMap<String, Integer> map = new HashMap<>();
        for (String s : titleProcess) {

            if (
                    Boolean.TRUE.equals(redisUtil.sIsMember(STOP_WORDS_KEY, s))
            ||
                   punctuationPattern.matcher(s).matches()
            ){
                //停用词，不算在内
                continue;
            }
            Integer i = map.get(s);
            if (i == null) {
                i = 1;
            } else {
                i++;
            }
            map.put(s, i);
        }
        return map;
    }
}
