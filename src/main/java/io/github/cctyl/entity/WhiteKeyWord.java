package io.github.cctyl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 白名单关键词
 * 它不能仅有关键词，还需要包含关键词对应的：
 * 分区名、
 * 标签名
 * 描述、
 * 标题、
 * 封面
 * 在以上条件中，包含两个以上，才算是真正匹配白名单
 */
@Data
@AllArgsConstructor
public class WhiteKeyWord {

    private String id;

    /**
     * 标签名
     */
    private List<String> tagNameList;

    /**
     * 视频描述应当包含的关键词
     */
    private List<String> descKeyWordList;


    /**
     * 视频标题应当包含的关键词
     */
    private List<String> titleKeyWordList;

    /**
     * 封面信息应当包含的关键词
     */
    private String coverKeyword;

    public WhiteKeyWord() {
        id = UUID.randomUUID().toString();
    }

    /**
     * 标题是否匹配了对应的关键词
     *
     * @param title
     * @return
     */
    public boolean titleMatch(String title) {
        //todo 同义词分析优化
        return titleKeyWordList.stream().anyMatch(title::contains);
    }

    /**
     * 标签是否匹配
     *
     * @return
     */
    public boolean tagNameMatch(List<String> tagNameListParam) {
        List<String> collect = new ArrayList<>(tagNameListParam);
        collect.retainAll(tagNameList);
        return collect.size() > 0;
    }


    /**
     * 描述是否匹配
     *
     * @return
     */
    public boolean descMatch(String desc) {
        return descKeyWordList.stream().anyMatch(desc::contains);
    }

    /**
     * 封面是否匹配
     *
     * @param picUrl
     * @return
     */
    public boolean coverMatch(String picUrl) {
        //todo 尚未实现
        return false;
    }
}
