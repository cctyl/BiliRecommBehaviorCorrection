package io.github.cctyl.entity;

import io.github.cctyl.utils.IdGenerator;
import io.github.cctyl.utils.SnowFlake;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;

/**
 * 白名单规则
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
@Accessors(chain = true)
public class WhitelistRule implements Serializable {

    private Long id;

    /**
     * 标签名
     */
    private Set<String> tagNameList = new HashSet<>();

    /**
     * 视频描述应当包含的关键词
     */
    private Set<String> descKeyWordList = new HashSet<>();


    /**
     * 视频标题应当包含的关键词
     */
    private Set<String> titleKeyWordList= new HashSet<>();

    /**
     * 封面信息应当包含的关键词
     */
    private String coverKeyword = "";

    public WhitelistRule() {
        id = IdGenerator.nextId();
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhitelistRule that = (WhitelistRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
