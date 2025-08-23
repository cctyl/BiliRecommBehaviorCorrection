package io.github.cctyl.domain.po;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@Accessors(chain = true)
public class WhiteListRule extends AuditingEntity implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 标签名
     */
    @TableField(exist = false)
    private List<Dict> tagNameList = new ArrayList<>();

    /**
     * 视频描述应当包含的关键词
     */
    @TableField(exist = false)
    private List<Dict> descKeyWordList = new ArrayList<>();


    /**
     * 视频标题应当包含的关键词
     */
    @TableField(exist = false)
    private List<Dict> titleKeyWordList= new ArrayList<>();

    /**
     * 封面信息应当包含的关键词
     */
    @TableField(exist = false)
    private List<Dict>  coverKeyword =  new ArrayList<>();


    private String info;


    @TableField(exist = false)
    @JsonIgnore
    private List<Dict>  totalDict =  new ArrayList<>();

    @JsonIgnore
    public List<Dict> getAggregationDict() {
        tagNameList = Opt.ofNullable(tagNameList).orElse(new LinkedList<>());
        descKeyWordList = Opt.ofNullable(descKeyWordList).orElse(new LinkedList<>());
        titleKeyWordList = Opt.ofNullable(titleKeyWordList).orElse(new LinkedList<>());
        coverKeyword = Opt.ofNullable(coverKeyword).orElse(new LinkedList<>());
        ArrayList<Dict> dicts = new ArrayList<>(
                       tagNameList.size()+
                       descKeyWordList.size()+
                       titleKeyWordList.size()+
                       coverKeyword.size()
        );
        dicts.addAll(tagNameList);
        dicts.addAll(descKeyWordList);
        dicts.addAll(titleKeyWordList);
        dicts.addAll(coverKeyword);

        dicts.forEach(dict -> dict.setOuterId(id));

        return dicts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhiteListRule that = (WhiteListRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
