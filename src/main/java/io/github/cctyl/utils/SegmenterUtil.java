package io.github.cctyl.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 结巴分词工具
 */
public class SegmenterUtil {

    private static final JiebaSegmenter jiebaSegmenter  = new JiebaSegmenter();


    /**
     * 分词
     * @param str
     * @return
     */
    public static List<String> process(String str){
        return  jiebaSegmenter.process(str, JiebaSegmenter.SegMode.SEARCH)
                .stream().map(segToken -> segToken.word).collect(Collectors.toList());
    }
}
